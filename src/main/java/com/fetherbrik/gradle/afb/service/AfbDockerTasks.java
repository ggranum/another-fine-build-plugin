package com.fetherbrik.gradle.afb.service;

import com.fetherbrik.gradle.afb.AnotherFineBuildPlugin;
import com.fetherbrik.gradle.afb.domain.DockerTag;
import com.fetherbrik.gradle.afb.domain.DockerInfo;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AfbDockerTasks {
  public final TaskProvider<Copy> dockerAssemble;
  public final TaskProvider<Exec> dockerBuild;
  public final TaskProvider<Task> dockerTag;
  public final List<TaskProvider<Exec>> dockerTags;
  public final Optional<TaskProvider<Task>> dockerLogin;
  // public final TaskProvider<Exec> dockerPush;
  public final TaskProvider<Task> dockerPushAllTags;
  public final List<TaskProvider<Exec>> dockerPushTags;

  public AfbDockerTasks(Project project, DockerInfo dockerInfo) {
      dockerAssemble = addDockerAssembleTask(project, dockerInfo);
      dockerBuild = addDockerBuildTask(project, dockerAssemble, dockerInfo);
      dockerTags = addDockerTagTasks(project, dockerBuild, dockerInfo);
      dockerTag = addDockerTagGroupTask(project, dockerTags, dockerBuild);
      dockerLogin = addDockerLoginTask(project, dockerInfo);
      // dockerPush = addDockerPushTask(project, dockerLogin, dockerTag, dockerInfo);
      dockerPushTags = addDockerPushTagTasks(project, dockerInfo.tags, dockerTag, dockerLogin, dockerInfo);
      dockerPushAllTags = addDockerPushTagsTask(project, dockerTag, dockerPushTags);
  }


  private TaskProvider<Copy> addDockerAssembleTask(Project project, DockerInfo docker) {
    return project.getTasks().register("dockerAssemble", Copy.class, assembleTask -> {
      assembleTask.setGroup(AnotherFineBuildPlugin.GROUP);
      assembleTask.setDescription("Assemble docker content for build.");
      assembleTask.from(docker.dockerFile);
      File dockerBuildDir = new File(project.getBuildDir(), docker.buildDir);
      assembleTask.into(dockerBuildDir);
      assembleTask.with(project.copySpec());
      assembleTask.getOutputs().file(new File(dockerBuildDir, docker.dockerFile));
      if(project.getPlugins().hasPlugin("application")){
        assembleTask.dependsOn("distTar");
      }
      assembleTask.dependsOn("build");
    });
  }

  private TaskProvider<Exec> addDockerBuildTask(Project project, TaskProvider<Copy> assembleTask, DockerInfo docker) {
    return project.getTasks().register("dockerBuild", Exec.class, dockerBuild -> {
      dockerBuild.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerBuild.setDescription("Build the primary docker image");
      dockerBuild.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      dockerBuild.setCommandLine("docker", "build", "--tag", docker.defaultTagPath(), ".");
      dockerBuild.dependsOn(assembleTask);
    });
  }

  // /** @todo ggranum: Make this tag and push latest or something. */
  // private TaskProvider<Exec> addDockerPushTask(Project project, Optional<TaskProvider<Task>> dockerLogin, TaskProvider<Task> dockerTag, DockerInfo docker) {
  //   return project.getTasks().register("dockerPush", Exec.class, dockerPush -> {
  //     dockerPush.setGroup(AnotherFineBuildPlugin.GROUP);
  //     dockerPush.setDescription("Push the default image (" + docker.defaultTagPath() + ") to the configured docker host. ");
  //     dockerPush.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
  //     dockerPush.setCommandLine("docker", "push", docker.defaultTagPath());
  //     dockerPush.dependsOn(dockerTag);
  //     dockerLogin.ifPresent(dockerPush::dependsOn);
  //   });
  // }

  /**
   * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each 'tag' task.
   */
  private TaskProvider<Task> addDockerTagGroupTask(Project project, List<TaskProvider<Exec>> dockerTags, TaskProvider<Exec> dockerBuild) {
    return project.getTasks().register("dockerTag", Task.class, dockerTag -> {
      dockerTag.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerTag.setDescription("Apply all configured tag tasks.");
      dockerTag.dependsOn(dockerBuild);
      for (TaskProvider<Exec> tag : dockerTags) {
        dockerTag.dependsOn(tag);
      }
    });
  }

  /**
   * Create the Tag task (the one that actually tags the image). Tag depends on the dockerBuild.
   */
  private List<TaskProvider<Exec>> addDockerTagTasks(Project project, TaskProvider<Exec> dockerBuild, DockerInfo docker) {
    List<TaskProvider<Exec>> result = new ArrayList<>();
    for (DockerTag tag : docker.tags) {
      result.add(addDockerTagTask(project, dockerBuild, docker, tag));
    }
    return result;
  }

  private TaskProvider<Exec> addDockerTagTask(Project project, TaskProvider<Exec> dockerBuild, DockerInfo docker, DockerTag tag) {
    return project.getTasks().register("dockerTag" + tag.capitalizedShortName(), Exec.class, (tagTask -> {
      tagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      tagTask.setDescription(tag.description);
      tagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      tagTask.setCommandLine("docker", "tag", docker.defaultTagPath(), docker.tagPath(tag));
      tagTask.dependsOn(dockerBuild);
    }));
  }

  /**
   * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each push task.
   */
  private TaskProvider<Task> addDockerPushTagsTask(Project project, TaskProvider<Task> dockerTag, List<TaskProvider<Exec>> dockerPushTags) {
    return project.getTasks().register("dockerPushTags", Task.class, pushTagsTask -> {
      pushTagsTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagsTask.setDescription("Push all configured tags, after pushing the main tag.");
      for (TaskProvider<Exec> tag : dockerPushTags) {
        pushTagsTask.dependsOn(tag);
      }
    });
  }

  /**
   * Create one "Push this tag" Task for each registered Docker Tag Task.
   */
  private List<TaskProvider<Exec>> addDockerPushTagTasks(Project project,
                                                         List<DockerTag> tags,
                                                         TaskProvider<Task> dockerTag,
                                                         Optional<TaskProvider<Task>> dockerLogin,
                                                         DockerInfo docker) {
    List<TaskProvider<Exec>> tasks = new ArrayList<>();
    for (DockerTag tag : tags) {
      tasks.add(addDockerPushTagTask(project, tag, dockerTag, dockerLogin, docker));
    }
    return tasks;
  }

  private TaskProvider<Exec> addDockerPushTagTask(Project project,
                                                  DockerTag tag,
                                                  TaskProvider<Task> dockerTag,
                                                  Optional<TaskProvider<Task>> dockerLogin,
                                                  DockerInfo docker) {
    return project.getTasks().register("dockerPush" + tag.capitalizedShortName(), Exec.class, pushTagTask -> {
      pushTagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagTask.setDescription("Push the image tag '" + tag.tag + "': " + tag.description);
      pushTagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      pushTagTask.setCommandLine("docker", "push", docker.tagPath(tag));
      pushTagTask.dependsOn(dockerTag);
      dockerLogin.ifPresent(pushTagTask::dependsOn);
    });
  }

  private Optional<TaskProvider<Task>> addDockerLoginTask(Project project, DockerInfo docker) {
    Optional<TaskProvider<Task>> result = Optional.empty();
    if (!docker.isLocal) {
      TaskProvider<Task> dockerLogin = project.getTasks().register("dockerLogin", Task.class, task -> {
        task.setGroup(AnotherFineBuildPlugin.GROUP);
        task.setDescription("Login to docker");
        task.doLast(t -> {
          if (!project.getRootProject().getExtensions().getExtraProperties().has("afb.dockerLoginHasRun")) {
            project.exec(execSpec -> {
              execSpec.executable("docker");
              if (docker.host.contains("hub.docker.com")) {
                execSpec.args("login", "-u", docker.username, "-p", docker.apiToken);
              } else {
                execSpec.args("login", "-u", docker.username, "-p", docker.apiToken, docker.host);
              }
            });
            project.getRootProject().getExtensions().getExtraProperties().set("afb.dockerLoginHasRun", true);
          }
        });
      });
      result = Optional.of(dockerLogin);
    }
    return result;
  }
}
