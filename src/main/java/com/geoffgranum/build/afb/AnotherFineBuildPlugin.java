package com.geoffgranum.build.afb;

import com.geoffgranum.build.VersionInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

public class AnotherFineBuildPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        AnotherFineBuildExtension extension = project.getExtensions().create("anotherFineBuild", AnotherFineBuildExtension.class, project);
        extension.getGit().setGitRoot(project.getRootDir().getAbsolutePath());
        AnotherFineBuildPlugin plugin = this;
        project.getTasks().register("afb", AnotherFineBuildTask.class, new Action<AnotherFineBuildTask>() {
            public void execute(AnotherFineBuildTask latestArtifactVersion) {
                extension.setInfo(plugin.hydrateVersionInfo(extension));
            }
        });
    }

    private VersionInfo hydrateVersionInfo(AnotherFineBuildExtension extension) {
        try {
            Map<String, Object> grGitOpenArgs = new HashMap<>();
            grGitOpenArgs.put("dir", extension.getGit().getGitRoot());
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(extension.getGit().getGitRoot()))
                                           .readEnvironment() // scan environment GIT_* variables
                                           .findGitDir() // scan up the file system tree
                                           .build();
            List<Ref> tags = repository.getRefDatabase().getRefsByPrefix(R_TAGS);
            Git git = new Git(repository);
            String describe = git.describe().call();
            VersionInfo info = new VersionInfo.Builder().isDirty(!git.status().call().isClean()).build();
            return info;
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}