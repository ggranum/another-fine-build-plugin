package com.geoffgranum.gradle.afb;

import com.geoffgranum.gradle.afb.service.AfbDockerTasks;
import com.geoffgranum.gradle.afb.service.AfbSemanticTasks;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

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
                // Only on root.
                AfbSemanticTasks semanticTasks = new AfbSemanticTasks();
                semanticTasks.applySemanticVersionTasks(p, extension.getInfo());

                AfbDockerTasks afbDocker = new AfbDockerTasks();
                afbDocker.applyDockerTasks(p, extension.getInfo());
                p.getChildProjects().forEach((String k, Project cp) -> {
                    afbDocker.applyDockerTasks(cp, extension.getInfo());
                });
            }
        });
    }
}