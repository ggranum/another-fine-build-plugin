package com.geoffgranum.gradle.afb;

import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.GitInfo;
import com.geoffgranum.gradle.afb.domain.configuration.ArtifactRepoConfig;
import com.geoffgranum.gradle.afb.domain.configuration.DockerConfig;
import com.geoffgranum.gradle.afb.domain.configuration.GitConfig;
import com.geoffgranum.gradle.afb.domain.configuration.ReleaseTarget;
import com.geoffgranum.gradle.afb.service.BuildInfoTransform;
import groovy.lang.Closure;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnotherFineBuildExtension {

    private final Project project;
    private final DockerConfig docker = new DockerConfig();
    private final GitConfig git = new GitConfig();
    private final ArtifactRepoConfig artifacts = new ArtifactRepoConfig();
    private final Map<String, ReleaseTarget> releaseTargets = new HashMap<>();
    private File versionInfoFilePath;
    private String buildType;
    private BuildInfo info;
    private Map<String, String> versions;

    public AnotherFineBuildExtension(Project project) {
        this.project = project;
    }

    public File getVersionInfoFilePath() {
        return versionInfoFilePath;
    }

    public void setVersionInfoFilePath(File versionInfoFilePath) {
        this.versionInfoFilePath = versionInfoFilePath;
    }

    @Input
    public Map<String, ReleaseTarget> getReleaseTargets() {
        return releaseTargets;
    }

    public void setReleaseTargets(Map<String, Closure<ReleaseTarget>> targets) {
        // this.releaseTargets = targets;
        for (Map.Entry<String, Closure<ReleaseTarget>> entry : targets.entrySet()) {
            ReleaseTarget t = new ReleaseTarget(entry.getKey());
            Closure<ReleaseTarget> value = entry.getValue();
            value.setDelegate(t);
            value.run();
            releaseTargets.put(t.getName(), t);
        }
    }

    public DockerConfig getDocker() {
        return docker;
    }

    public GitConfig getGit() {
        return git;
    }

    public void git(Action<? super GitConfig> action) {
        action.execute(git);
    }

    public void docker(Action<? super DockerConfig> action) {
        action.execute(docker);
    }

    public void artifacts(Action<? super ArtifactRepoConfig> action) {
        action.execute(artifacts);
    }

    public ArtifactRepoConfig artifacts() {
        return artifacts;
    }

    public ArtifactRepoConfig getArtifacts() {
        return artifacts;
    }

    public BuildInfo getInfo() {
        if (info == null) {
            GitInfo git = hydrateVersionInfo(this);
            info = new BuildInfoTransform(this, git).apply(project);
        }
        return info;
    }

    public void setInfo(BuildInfo buildInfo) {
        this.info = buildInfo;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public Map<String, String> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, String> versions) {
        this.versions = versions;
    }

    private GitInfo hydrateVersionInfo(AnotherFineBuildExtension extension) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.findGitDir(new File(extension.getGit().getGitRoot())).readEnvironment() // scan environment GIT_* variables
                                           .findGitDir() // scan up the file system tree
                                           .build();
            if (repository == null || repository.isBare()) {
                throw new RuntimeException("Another Fine Build requires a project that is part of a working git repository.");
            }
            if (repository.resolve("HEAD") == null) {
                throw new RuntimeException("Another Fine Build requires a project that is part of a working git repository. Have you made any commits?");
            }
            String hash = repository.resolve("HEAD").name();
            Git git = new Git(repository);
            String describe = git.describe().setLong(true).setAlways(true).call();
            return new GitInfo.Builder()
                    .gitRoot(extension.getGit().getGitRoot())
                    .hash(hash)
                    .isDirty(!git.status().call().isClean())
                    .describe(describe)
                    .branchName(repository.getBranch())
                    .build();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
