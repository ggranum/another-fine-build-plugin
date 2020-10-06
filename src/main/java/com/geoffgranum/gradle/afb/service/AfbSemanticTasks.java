package com.geoffgranum.gradle.afb.service;

import com.geoffgranum.gradle.afb.AnotherFineBuildPlugin;
import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.VersionInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class AfbSemanticTasks {
  public void applySemanticVersionTasks(Project project, BuildInfo info) {
    addVersionPatchTask(project, info);
  }

  private DefaultTask addVersionPatchTask(Project project, BuildInfo info) {
    return project.getTasks().create("versionPatch", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription("Update the Patch revision and commit. Requires clean git workspace");
      buildTask.getActions().add((task) -> {
        try {
          File versionFile = checkVersionFileCanBeCreated(info.versionInfoFilePath);
          FileRepositoryBuilder builder = new FileRepositoryBuilder();
          Repository repository = builder.findGitDir(new File(info.git.gitRoot)).readEnvironment() // scan environment GIT_* variables
                                         .findGitDir() // scan up the file system tree
                                         .build();
          Git git = new Git(repository);
          if (!git.status().call().isClean()) {
            throw new RuntimeException("Your git workspace must be clean to perform a version update.");
          }
          VersionInfo next = info.version.nextPatch();
          Files.writeString(versionFile.toPath(), next.full, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
          Path base = repository.getDirectory().getParentFile().toPath();
          String relativePath = base.relativize(versionFile.toPath()).toString();
          git.add().addFilepattern(relativePath).call();
          git.commit().setMessage(String.format("Update revision from '%s' to '%s'.", info.version.full, next.full)).call();
          project.getLogger().quiet("Adding '" + relativePath + "' to git for commit.");
          git.tag().setAnnotated(true).setName(next.full).setMessage("Update patch revision").call();
        } catch (IOException | GitAPIException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  private File checkVersionFileCanBeCreated(Optional<File> path) {
    if (path.isEmpty()) {
      throw new RuntimeException("afb.versionInfoFilePath must be set to apply versions.");
    }
    File parentFile = path.get().getParentFile();
    if (!parentFile.exists()) {
      String msg = String.format("afb.versionInfoFilePath must point to an existing directory. Current value: '%s'.", parentFile.getAbsolutePath());
      throw new RuntimeException(msg);
    }
    return path.get();
  }
}
