package com.geoffgranum.build.configuration;

public class ReleaseTarget {
    private String name;
    private String matchingVersionsRegex;
    private boolean artifacts;
    private boolean docker;
    private String dockerTag;

    public ReleaseTarget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public String getMatchingVersionsRegex() {
        return matchingVersionsRegex;
    }

    public void setMatchingVersionsRegex(String matchingVersionsRegex) {
        this.matchingVersionsRegex = matchingVersionsRegex;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppEnvironment{");
        sb.append("name='").append(name).append('\'');
        sb.append(", matchingVersionsRegex='").append(matchingVersionsRegex).append('\'');
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
}
