package com.fetherbrik.gradle.afb.domain.configuration;

public class ReleaseTarget {
    private String name;
    private String versionMatches;
    private String buildTypeMatches;
    private boolean artifacts;
    private boolean isSnapshot;
    private boolean docker;
    private String dockerTag;

    public ReleaseTarget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSnapshot() {
        return isSnapshot;
    }

    public void setSnapshot(boolean snapshot) {
        isSnapshot = snapshot;
    }

    public String getBuildTypeMatches() {
        return buildTypeMatches;
    }

    public void setBuildTypeMatches(String buildTypeMatches) {
        this.buildTypeMatches = buildTypeMatches;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArtifacts() {
        return artifacts;
    }

    public void setArtifacts(boolean artifacts) {
        this.artifacts = artifacts;
    }

    public boolean isDocker() {
        return docker;
    }

    public void setDocker(boolean docker) {
        this.docker = docker;
    }

    public String getVersionMatches() {
        return versionMatches;
    }

    public void setVersionMatches(String versionMatches) {
        this.versionMatches = versionMatches;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppEnvironment{");
        sb.append("name='").append(name).append('\'');
        sb.append(", matchingVersionsRegex='").append(versionMatches).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getDockerTag() {
        if(this.dockerTag == null){
            this.dockerTag = name;
        }
        return dockerTag;
    }

    public void setDockerTag(String dockerTag) {
        this.docker = true;
        this.dockerTag = dockerTag;
    }

    public boolean matches(String buildType, String version) {
        boolean matches = false;
        if(version.matches(this.versionMatches)){
            matches = this.buildTypeMatches == null || buildType.matches(buildTypeMatches);
        }
        return  matches;
    }
}
