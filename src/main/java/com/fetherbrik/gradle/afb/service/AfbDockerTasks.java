package com.fetherbrik.gradle.afb.service;

import com.fetherbrik.gradle.afb.AnotherFineBuildPlugin;
import com.fetherbrik.gradle.afb.domain.BuildInfo;
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
  public final TaskProvider<Exec> dockerPush;
  public final TaskProvider<Task> dockerPushAllTags;
  public final List<TaskProvider<Exec>> dockerPushTags;

  public AfbDockerTasks(Project project, BuildInfo info) {
      dockerAssemble = addDockerAssembleTask(project, info);
      dockerBuild = addDockerBuildTask(project, dockerAssemble, info);
      dockerTags = addDockerTagTasks(project, info);
      dockerTag = addDockerTagGroupTask(project, dockerTags, dockerBuild);
      dockerLogin = addDockerLoginTask(project, info);
      dockerPush = addDockerPushTask(project, dockerLogin, dockerTag, info);
      dockerPushTags = addDockerPushTagTasks(project, info.docker.tags, dockerPush, info);
      dockerPushAllTags = addDockerPushTagsTask(project, dockerPush, dockerPushTags);
  }


  private TaskProvider<Copy> addDockerAssembleTask(Project project, BuildInfo info) {
    DockerInfo docker = info.docker;
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

  private TaskProvider<Exec> addDockerBuildTask(Project project, TaskProvider<Copy> assembleTask, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().register("dockerBuild", Exec.class, dockerBuild -> {
      dockerBuild.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerBuild.setDescription("Build the primary docker image");
      dockerBuild.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      dockerBuild.setCommandLine("docker", "build", "--tag", docker.defaultTagPath(), ".");
      dockerBuild.dependsOn(assembleTask);
    });
  }

  private TaskProvider<Exec> addDockerPushTask(Project project, Optional<TaskProvider<Task>> dockerLogin, TaskProvider<Task> dockerTag, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().register("dockerPush", Exec.class, dockerPush -> {
      dockerPush.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerPush.setDescription("Push the image to the configured docker host.");
      dockerPush.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      dockerPush.setCommandLine("docker", "push", docker.defaultTagPath());
      dockerPush.dependsOn(dockerTag);
      if (dockerLogin.isPresent()) {
        dockerPush.dependsOn(dockerLogin.get());
      }
    });
  }

  /**
   * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each 'tag' task.
   * @return
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

  private List<TaskProvider<Exec>> addDockerTagTasks(Project project, BuildInfo info) {
    List<TaskProvider<Exec>> result = new ArrayList<>();
    DockerInfo docker = info.docker;
    for (DockerTag tag : docker.tags) {
      result.add(addDockerTagTask(project, docker, tag));
    }
    return result;
  }

  private TaskProvider<Exec> addDockerTagTask(Project project, DockerInfo docker, DockerTag tag) {
    return project.getTasks().register("dockerTag" + tag.capitalizedShortName(), Exec.class, (tagTask -> {
      tagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      tagTask.setDescription(tag.description);
      tagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      tagTask.setCommandLine("docker", "tag", docker.defaultTagPath(), docker.tagPath(tag));
    }));
  }

  /**
   * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each push task.
   */
  private TaskProvider<Task> addDockerPushTagsTask(Project project, TaskProvider<Exec> dockerPush, List<TaskProvider<Exec>> dockerPushTags) {
    return project.getTasks().register("dockerPushTags", Task.class, pushTagsTask -> {
      pushTagsTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagsTask.setDescription("Push all configured tags, after pushing the main tag.");
      pushTagsTask.dependsOn(dockerPush);
      for (TaskProvider<Exec> tag : dockerPushTags) {
        pushTagsTask.dependsOn(tag);
      }
    });
  }

  private List<TaskProvider<Exec>> addDockerPushTagTasks(Project project, List<DockerTag> tags, TaskProvider<Exec> dockerPush, BuildInfo info) {
    List<TaskProvider<Exec>> tasks = new ArrayList<>();
    for (DockerTag tag : tags) {
      tasks.add(addDockerPushTagTask(project, tag, dockerPush, info));
    }
    return tasks;
  }

  private TaskProvider<Exec> addDockerPushTagTask(Project project, DockerTag tag, TaskProvider<Exec> dockerPush, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().register("dockerPush" + tag.capitalizedShortName(), Exec.class, pushTagTask -> {
      pushTagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagTask.setDescription("Push the image tag '" + tag.tag + "': " + tag.description);
      pushTagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      pushTagTask.setCommandLine("docker", "push", docker.tagPath(tag));
      pushTagTask.dependsOn(dockerPush);
    });
  }

  private Optional<TaskProvider<Task>> addDockerLoginTask(Project project, BuildInfo info) {
    Optional<TaskProvider<Task>> result = Optional.empty();
    if (!info.docker.isLocal) {
      DockerInfo docker = info.docker;
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
