package com.fetherbrik.gradle.afb.domain;

import com.fetherbrik.gradle.afb.domain.configuration.ReleaseTarget;

import java.io.File;
import java.util.Optional;

public final class BuildInfo {

  public final String dateStamp;
  public final ReleaseTarget target;
  public final VersionInfo version;
  public final GitInfo git;
  public final DockerInfo docker;
  public final Optional<File> versionInfoFilePath;

  private BuildInfo(Builder builder) {
    dateStamp = builder.dateStamp;
    target = builder.target;
    version = builder.version;
    git = builder.git;
    docker = builder.docker;
    versionInfoFilePath = Optional.ofNullable(builder.versionInfoFilePath);
  }

  public boolean dockerEnabled() {
    return this.docker.enabled;
  }

  public static final class Builder {
    private String dateStamp;
    private ReleaseTarget target;
    private VersionInfo version;
    private GitInfo git;
    private DockerInfo docker;
    private File versionInfoFilePath;

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

    public Builder versionInfoFilePath(File versionInfoFilePath) {
      this.versionInfoFilePath = versionInfoFilePath;
      return this;
    }

    public Builder from(BuildInfo copy) {
      dateStamp = copy.dateStamp;
      target = copy.target;
      version = copy.version;
      git = copy.git;
      docker = copy.docker;
      versionInfoFilePath = copy.versionInfoFilePath.orElse(null);
      return this;
    }

    public Builder copy() {
      Builder copy = new Builder();
      copy.dateStamp(this.dateStamp);
      copy.target(this.target);
      copy.version(this.version);
      copy.git(this.git);
      copy.docker(this.docker);
      copy.versionInfoFilePath(this.versionInfoFilePath);
      return copy;
    }

    public BuildInfo build() {
      return new BuildInfo(this);
    }
  }
}
