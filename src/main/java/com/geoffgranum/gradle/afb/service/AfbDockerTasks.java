package com.geoffgranum.gradle.afb.service;

import com.geoffgranum.gradle.afb.AnotherFineBuildPlugin;
import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.DockerInfo;
import com.geoffgranum.gradle.afb.domain.DockerTag;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AfbDockerTasks {
  public final Copy dockerAssemble;
  public final Exec dockerBuild;
  public final Task dockerTag;
  public final List<Exec> dockerTags;
  public final Optional<Task> dockerLogin;
  public final Exec dockerPush;
  public final Task dockerPushAllTags;
  public final List<Exec> dockerPushTags;

  public AfbDockerTasks(Project project, BuildInfo info) {
      dockerAssemble = addDockerAssembleTask(project, info);
      dockerBuild = addDockerBuildTask(project, dockerAssemble, info);
      dockerTag = addDockerTagGroupTask(project, dockerBuild);
      dockerTags = addDockerTagTasks(project, dockerTag, info);
      dockerLogin = addDockerLoginTask(project, info);
      dockerPush = addDockerPushTask(project, dockerLogin, dockerTag, info);
      dockerPushAllTags = addDockerPushTagsTask(project, dockerPush);
      dockerPushTags = addDockerPushTagTasks(project, info.docker.tags, dockerPush, dockerPushAllTags, info);
  }


  private Copy addDockerAssembleTask(Project project, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().create("dockerAssemble", Copy.class, assembleTask -> {
      assembleTask.setGroup(AnotherFineBuildPlugin.GROUP);
      assembleTask.setDescription("Assemble docker content for build.");
      assembleTask.from(docker.dockerFile);
      assembleTask.into(new File(project.getBuildDir(), docker.buildDir));
      assembleTask.with(project.copySpec());
    });
  }

  private Exec addDockerBuildTask(Project project, Copy assembleTask, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().create("dockerBuild", Exec.class, dockerBuild -> {
      dockerBuild.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerBuild.setDescription("Build the primary docker image");
      dockerBuild.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      dockerBuild.setCommandLine("docker", "build", "--tag", docker.defaultTagPath(), ".");
      dockerBuild.dependsOn(assembleTask);
    });
  }

  private Exec addDockerPushTask(Project project, Optional<Task> dockerLogin, Task dockerTag, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().create("dockerPush", Exec.class, dockerPush -> {
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
   */
  private Task addDockerTagGroupTask(Project project, Exec dockerBuild) {
    return project.getTasks().create("dockerTag", Task.class, dockerTag -> {
      dockerTag.setGroup(AnotherFineBuildPlugin.GROUP);
      dockerTag.setDescription("Apply all configured tag tasks.");
      dockerTag.dependsOn(dockerBuild);
    });
  }

  private List<Exec> addDockerTagTasks(Project project, Task dockerTags, BuildInfo info) {
    List<Exec> result = new ArrayList<>();
    DockerInfo docker = info.docker;
    for (DockerTag tag : docker.tags) {
      result.add(addDockerTagTask(project, dockerTags, docker, tag));
    }
    return result;
  }

  private Exec addDockerTagTask(Project project, Task dockerApplyAllTags, DockerInfo docker, DockerTag tag) {
    Exec task = project.getTasks().create("dockerTag" + tag.capitalizedShortName(), Exec.class, (tagTask -> {
      tagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      tagTask.setDescription(tag.description);
      tagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      tagTask.setCommandLine("docker", "tag", docker.defaultTagPath(), docker.tagPath(tag));
      dockerApplyAllTags.dependsOn(tagTask);
    }));
    return task;
  }

  /**
   * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each push task.
   */
  private Task addDockerPushTagsTask(Project project, Exec dockerPush) {
    return project.getTasks().create("dockerPushTags", Task.class, pushTagsTask -> {
      pushTagsTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagsTask.setDescription("Push all configured tags, after pushing the main tag.");
      pushTagsTask.dependsOn(dockerPush);
    });
  }

  private List<Exec> addDockerPushTagTasks(Project project, List<DockerTag> tags, Task dockerPush, Task dockerPushTags, BuildInfo info) {
    List<Exec> tasks = new ArrayList<>();
    for (DockerTag tag : tags) {
      tasks.add(addDockerPushTagTask(project, tag, dockerPush, dockerPushTags, info));
    }
    return tasks;
  }

  private Exec addDockerPushTagTask(Project project, DockerTag tag, Task dockerPush, Task dockerPushTags, BuildInfo info) {
    DockerInfo docker = info.docker;
    return project.getTasks().create("dockerPush" + tag.capitalizedShortName(), Exec.class, pushTagTask -> {
      pushTagTask.setGroup(AnotherFineBuildPlugin.GROUP);
      pushTagTask.setDescription("Push the image tag '" + tag.tag + "': " + tag.description);
      pushTagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
      pushTagTask.setCommandLine("docker", "push", docker.tagPath(tag));
      pushTagTask.dependsOn(dockerPush);
      dockerPushTags.dependsOn(pushTagTask);
    });
  }

  private Optional<Task> addDockerLoginTask(Project project, BuildInfo info) {
    Optional<Task> result = Optional.empty();
    if (!info.docker.isLocal) {
      DockerInfo docker = info.docker;
      Task dockerLogin = project.getTasks().create("dockerLogin", Task.class, task -> {
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
