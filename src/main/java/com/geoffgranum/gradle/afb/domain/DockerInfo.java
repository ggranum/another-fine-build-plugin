package com.geoffgranum.gradle.afb.domain;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class DockerInfo {

    public final String host;
    public final String repo;
    public final boolean isLocal;
    public final boolean isHub;

    public final String username;
    public final String apiToken;
    public final File dockerFile;
    public final File buildDir;
    public final List<DockerTag> tags;

    public final String versionString;
    public final String dateStamp;

    private DockerInfo(Builder builder) {
        host = builder.host;
        repo = builder.repo;
        isLocal = builder.isLocal;
        isHub = builder.isHub;
        username = builder.username;
        apiToken = builder.apiToken;
        dockerFile = builder.dockerFile;
        buildDir = builder.buildDir;
        tags = ImmutableList.copyOf(builder.tags);
        versionString = builder.versionString;
        dateStamp = builder.dateStamp;
    }

    public String baseRepoTag(){
        return this.isHub || this.isLocal ? this.repo : this.host + "/" + this.repo;
    }

    public static final class Builder {
        private String host;
        private String repo;
        private Boolean isLocal = false;
        private Boolean isHub = false;
        private String username;
        private String apiToken;
        private File dockerFile;
        private File buildDir;
        private List<DockerTag> tags = Collections.emptyList();
        private String versionString;
        private String dateStamp;

        public Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder isLocal(boolean isLocal) {
            this.isLocal = isLocal;
            return this;
        }

        public Builder isHub(boolean isHub) {
            this.isHub = isHub;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder dockerFile(File dockerFile) {
            this.dockerFile = dockerFile;
            return this;
        }

        public Builder buildDir(File buildDir) {
            this.buildDir = buildDir;
            return this;
        }

        public Builder tags(List<DockerTag> tags) {
            this.tags = tags;
            return this;
        }

        public Builder versionString(String versionString) {
            this.versionString = versionString;
            return this;
        }

        public Builder dateStamp(String dateStamp) {
            this.dateStamp = dateStamp;
            return this;
        }

        public DockerInfo build() {
            return new DockerInfo(this);
        }
    }
}
