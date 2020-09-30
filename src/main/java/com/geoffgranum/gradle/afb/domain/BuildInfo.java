package com.geoffgranum.gradle.afb.domain;

import com.geoffgranum.gradle.afb.domain.configuration.ReleaseTarget;


public final class BuildInfo {

    public final String dateStamp;
    public final ReleaseTarget target;
    public final VersionInfo version;
    public final GitInfo git;
    public final DockerInfo docker;

    private BuildInfo(Builder builder) {
        dateStamp = builder.dateStamp;
        target = builder.target;
        version = builder.version;
        git = builder.git;
        docker = builder.docker;
    }

    public static final class Builder {
        private String dateStamp;
        private ReleaseTarget target;
        private VersionInfo version;
        private GitInfo git;
        private DockerInfo docker;

        public Builder() {
        }

        public Builder dateStamp(String dateStamp) {
            this.dateStamp = dateStamp;
            return this;
        }

        public Builder target(ReleaseTarget target) {
            this.target = target;
            return this;
        }

        public Builder version(VersionInfo version) {
            this.version = version;
            return this;
        }

        public Builder git(GitInfo git) {
            this.git = git;
            return this;
        }

        public Builder docker(DockerInfo docker) {
            this.docker = docker;
            return this;
        }

        public BuildInfo build() {
            return new BuildInfo(this);
        }
    }
}
