package com.fetherbrik.gradle.afb;

import com.fetherbrik.gradle.afb.service.AfbSemanticTasks;
import com.fetherbrik.gradle.afb.service.AfbCoreTasks;
import com.fetherbrik.gradle.afb.service.AfbDockerTasks;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

public class AnotherFineBuildPlugin implements Plugin<Project> {

  public static final String GROUP = "afb";

  @Override
  public void apply(final Project project) {
    AnotherFineBuildExtension extension = project.getExtensions().create("afb", AnotherFineBuildExtension.class, project);
    extension.getGit().setGitRoot(project.getRootDir().getAbsolutePath());
    project.getTasks().register("afb", AnotherFineBuildTask.class, latestArtifactVersion -> {
      try {
        extension.getInfo();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    project.afterEvaluate(p -> {
      if (extension.getInfo() != null) {
        AfbSemanticTasks semanticTasks = new AfbSemanticTasks(p, extension.getInfo());
        if (new File(project.getProjectDir(), extension.getInfo().docker.dockerFile).exists()) {
          AfbDockerTasks afbDocker = new AfbDockerTasks(project, extension.getInfo());
        }
        AfbCoreTasks coreTasks = new AfbCoreTasks(p, extension.getInfo());
        p.getChildProjects().forEach((String k, Project cp) -> {
          if (new File(cp.getProjectDir(), extension.getInfo().docker.dockerFile).exists()) {
            AfbDockerTasks childDocker = new AfbDockerTasks(cp, extension.getInfo());
          }
          AfbCoreTasks childCoreTasks = new AfbCoreTasks(cp, extension.getInfo());
        });
      }
    });
  }
}
