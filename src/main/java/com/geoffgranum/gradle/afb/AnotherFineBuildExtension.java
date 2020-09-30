package com.geoffgranum.gradle.afb;

import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.configuration.ArtifactRepoConfig;
import com.geoffgranum.gradle.afb.domain.configuration.DockerConfig;
import com.geoffgranum.gradle.afb.domain.configuration.GitConfig;
import com.geoffgranum.gradle.afb.domain.configuration.ReleaseTarget;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AnotherFineBuildExtension {

    private File versionInfoFilePath;
    private String buildType;
    private final Project project;
    private final DockerConfig docker = new DockerConfig();
    private final GitConfig git = new GitConfig();
    private final ArtifactRepoConfig artifacts = new ArtifactRepoConfig();
    private final Map<String, ReleaseTarget> releaseTargets = new HashMap<>();

    private BuildInfo info;

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

    public void setInfo(BuildInfo buildInfo) {
        this.info = buildInfo;
    }

    public BuildInfo getInfo() {
        return info;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

}
