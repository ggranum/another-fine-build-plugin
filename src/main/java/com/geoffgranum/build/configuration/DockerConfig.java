package com.geoffgranum.build.configuration;

public class DockerConfig {
    private String repositoryHost;
    private String repositoryOrg;
    private String username;
    private String apiToken;

    public String getRepositoryHost() {
        return repositoryHost;
    }

    public void setRepositoryHost(String repositoryHost) {
        this.repositoryHost = repositoryHost;
    }

    public String getRepositoryOrg() {
        return repositoryOrg;
    }

    public void setRepositoryOrg(String repositoryOrg) {
        this.repositoryOrg = repositoryOrg;
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
}
