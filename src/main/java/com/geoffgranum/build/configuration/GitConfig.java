package com.geoffgranum.build.configuration;

public class GitConfig {
    private String username;
    private String apiKey;
    private String gitRoot = ".";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getGitRoot() {
        return gitRoot;
    }

    public void setGitRoot(String gitRoot) {
        this.gitRoot = gitRoot;
    }
}
