package com.geoffgranum.gradle.afb;

import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.GitInfo;
import com.geoffgranum.gradle.afb.service.AfbDocker;
import com.geoffgranum.gradle.afb.service.BuildInfoTransform;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

public class AnotherFineBuildPlugin implements Plugin<Project> {

    public static final String GROUP = "afb";

    @Override
    public void apply(final Project project) {
        AnotherFineBuildExtension extension = project.getExtensions().create("afb", AnotherFineBuildExtension.class, project);
        extension.getGit().setGitRoot(project.getRootDir().getAbsolutePath());
        AnotherFineBuildPlugin plugin = this;
        project.getTasks().register("anotherFineBuild", AnotherFineBuildTask.class, new Action<AnotherFineBuildTask>() {
            public void execute(AnotherFineBuildTask latestArtifactVersion) {
                try {
                    GitInfo git = plugin.hydrateVersionInfo(extension);
                    BuildInfo info = new BuildInfoTransform(extension, git).apply(project);
                    extension.setInfo(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        project.afterEvaluate(p -> {
            if(extension.getInfo() != null) {
                new AfbDocker().applyDockerTasks(p, extension.getInfo());
            }

        });
    }



    private GitInfo hydrateVersionInfo(AnotherFineBuildExtension extension) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.findGitDir(new File(extension.getGit().getGitRoot())).readEnvironment() // scan environment GIT_* variables
                                           .findGitDir() // scan up the file system tree
                                           .build();
            String hash = repository.resolve("HEAD").name();
            Git git = new Git(repository);
            String describe = git.describe().setLong(true).setAlways(true).call();
            return new GitInfo.Builder().hash(hash).isDirty(!git.status().call().isClean()).describe(describe).branchName(repository.getBranch()).build();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}