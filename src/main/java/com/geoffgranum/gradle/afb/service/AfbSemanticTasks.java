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
  public AfbSemanticTasks(Project project, BuildInfo info) {
    addVersionSuffixTask(project, info);
    addVersionPatchTask(project, info);
    addVersionMinorTask(project, info);
    addVersionMajorTask(project, info);
  }

  private DefaultTask addVersionSuffixTask(Project project, BuildInfo info) {
    return project.getTasks().create("versionSuffix", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription("Update the Suffix and commit. Requires clean git workspace. Use -Psuffix=newSuffix to specify new suffix.");
      buildTask.getActions().add((task) -> {
        try {
          VersionInfo next;
          File versionFile = checkVersionFileCanBeCreated(info.versionInfoFilePath);
          if (!project.hasProperty("suffix")) {
            throw new RuntimeException("Suffix argument is required: use './gradlew versionSuffix -Psuffix=newSuffix'");
          }
          String suffix = (String) project.getProperties().get("suffix");
          next = info.version.copy().suffix(suffix).build();
          if (info.version.full.equals(next.full)) {
            throw new RuntimeException("Suffix has not changed, nothing to do.");
          }
          applyNextVersion(project, info, next, versionFile);
        } catch (IOException | GitAPIException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  private DefaultTask addVersionPatchTask(Project project, BuildInfo info) {
    return project.getTasks().create("versionPatch", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription("Update the Patch revision and commit. Requires clean git workspace. Use -Psuffix=newSuffix to update suffix.");
      buildTask.getActions().add((task) -> {
        try {
          VersionInfo next;
          File versionFile = checkVersionFileCanBeCreated(info.versionInfoFilePath);
          if (project.hasProperty("suffix")) {
            String suffix = (String) project.getProperties().get("suffix");
            next = info.version.nextPatch(suffix);
          } else {
            next = info.version.nextPatch();
          }
          applyNextVersion(project, info, next, versionFile);
        } catch (IOException | GitAPIException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  private DefaultTask addVersionMinorTask(Project project, BuildInfo info) {
    return project.getTasks().create("versionMinor", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription(
        "Update the Minor revision number, setting patch to '0', and commit. Requires clean git workspace. Use -Psuffix=newSuffix to update suffix.");
      buildTask.getActions().add((task) -> {
        try {
          VersionInfo next;
          File versionFile = checkVersionFileCanBeCreated(info.versionInfoFilePath);
          if (project.hasProperty("suffix")) {
            String suffix = (String) project.getProperties().get("suffix");
            next = info.version.nextMinor(suffix);
          } else {
            next = info.version.nextMinor();
          }
          applyNextVersion(project, info, next, versionFile);
        } catch (IOException | GitAPIException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  private DefaultTask addVersionMajorTask(Project project, BuildInfo info) {
    return project.getTasks().create("versionMajor", DefaultTask.class, buildTask -> {
      buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
      buildTask.setDescription(
        "Update the Major revision number, setting minor and patch to '0', and commit. Requires clean git workspace. Use -Psuffix=newSuffix to update suffix.");
      buildTask.getActions().add((task) -> {
        try {
          VersionInfo next;
          File versionFile = checkVersionFileCanBeCreated(info.versionInfoFilePath);
          if (project.hasProperty("suffix")) {
            String suffix = (String) project.getProperties().get("suffix");
            next = info.version.nextMajor(suffix);
          } else {
            next = info.version.nextMajor();
          }
          applyNextVersion(project, info, next, versionFile);
        } catch (IOException | GitAPIException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  private void applyNextVersion(Project project, BuildInfo info, VersionInfo next, File versionFile) throws IOException, GitAPIException {
    Repository repository = new FileRepositoryBuilder().findGitDir(new File(info.git.gitRoot)).build();
    Git git = new Git(repository);
    if (!git.status().call().isClean()) {
      throw new RuntimeException("Your git workspace must be clean to perform a version update.");
    }

    Files.writeString(versionFile.toPath(), next.full, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    Path base = repository.getDirectory().getParentFile().toPath();
    String relativePath = base.relativize(versionFile.toPath()).toString();

    git.add().addFilepattern(relativePath).call();
    git.commit().setMessage(String.format("Update revision from '%s' to '%s'.", info.version.full, next.full)).call();
    project.getLogger().quiet("Adding '" + relativePath + "' to git for commit.");
    git.tag().setAnnotated(true).setName(next.full).setMessage("Update patch revision").call();
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
