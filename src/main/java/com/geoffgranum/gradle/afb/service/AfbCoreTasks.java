package com.geoffgranum.gradle.afb.service;

import com.geoffgranum.gradle.afb.AnotherFineBuildPlugin;
import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.configuration.ReleaseTarget;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class AfbCoreTasks {
  public final TaskProvider<DefaultTask> buildCurrentTarget;

  public AfbCoreTasks(Project project, BuildInfo info) {
    buildCurrentTarget = addBuildCurrentTargetTask(project, info);
  }

  private TaskProvider<DefaultTask> addBuildCurrentTargetTask(Project project, BuildInfo info) {
    return project.getTasks().register("buildCurrentTarget", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription("Builds the current target, based on the configuration specified in the afb task.");
      ReleaseTarget target = info.target;
      project.getLogger().quiet("AFB: checking for tasks to add to chain for target '" + target.getName() + "' on project '" + project.getName() +  "'.");
      if(project.getPlugins().hasPlugin("application")){
        project.getLogger().quiet("AFB: Adding distTar task to chain");
        buildTask.dependsOn("distTar");
      }
      else if(project.getPlugins().hasPlugin("java")){
        project.getLogger().quiet("AFB: Adding java build task to chain");
        buildTask.dependsOn("build");
        buildTask.dependsOn("test");
      }
      if (target.isArtifacts() && project.getPlugins().hasPlugin("maven-publish")) {
        project.getLogger().quiet("AFB: Adding publish task to chain");
        buildTask.dependsOn("publish");
      }
      if (target.isDocker() && info.docker.hasDockerFile(project)) {
        project.getLogger().quiet("AFB: Adding dockerBuild & Push tasks to chain");
        buildTask.dependsOn("dockerPushTags");
      }
    });
  }
}
