package com.geoffgranum.gradle.afb.domain.configuration;

import groovy.lang.Closure;

public class DockerConfig {
    private String repoHost;
    private String repoOrg;
    private Closure<String> repoName;
    private String username;
    private String apiToken;

    public String getRepoHost() {
        return repoHost;
    }

    public void setRepoHost(String repoHost) {
        this.repoHost = repoHost;
    }

    public Closure<String> getRepoName() {
        return repoName;
    }

    public void setRepoName(Closure<String> repoName) {
        this.repoName = repoName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getRepoOrg() {
        return repoOrg;
    }

    public void setRepoOrg(String repoOrg) {
        this.repoOrg = repoOrg;
    }
}
