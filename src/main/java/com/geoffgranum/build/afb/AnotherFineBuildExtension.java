package com.geoffgranum.build.afb;

import com.geoffgranum.build.VersionInfo;
import com.geoffgranum.build.configuration.ArtifactRepoConfig;
import com.geoffgranum.build.configuration.DockerConfig;
import com.geoffgranum.build.configuration.GitConfig;
import com.geoffgranum.build.configuration.ReleaseTarget;
import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AnotherFineBuildExtension {

    private final Project project;
    private final DockerConfig docker = new DockerConfig();
    private final GitConfig git = new GitConfig();
    private final ArtifactRepoConfig artifacts = new ArtifactRepoConfig();
    private final Map<String, ReleaseTarget> releaseTargets = new HashMap<>();
    private String currentVersion;
    private VersionInfo versionInfo;

    public AnotherFineBuildExtension(Project project) {
        this.project = project;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }


    public void setCurrentVersion(File currentVersion) throws IOException {
        this.currentVersion = FileUtils.readFileToString(currentVersion, StandardCharsets.UTF_8).trim();
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

    public void setInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }
}
