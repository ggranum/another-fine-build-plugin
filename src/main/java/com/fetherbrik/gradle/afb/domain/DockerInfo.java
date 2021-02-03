package com.fetherbrik.gradle.afb.domain;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DockerInfo {

  public final String host;
  public final String repo;
  public final Optional<String> org;
  public final boolean isLocal;
  public final boolean isHub;

  public final String username;
  public final String apiToken;
  public final String dockerFile;
  public final String buildDir;
  public final List<DockerTag> tags;

  public final String versionString;
  public final String dateStamp;
  public final boolean enabled;

  private DockerInfo() {
    this(new Builder().enabled(false));
  }

  private DockerInfo(Builder builder) {
    enabled = builder.enabled;
    host = builder.host;
    repo = builder.repo;
    org = Optional.ofNullable(builder.org);
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

  public static DockerInfo disabled() {
    return new DockerInfo();
  }

  /** @todo ggranum: Fix this. If you push this 'tag', all the tags under the repo are pushed.  */
  public String defaultTagPath() {
    return this.isHub || this.isLocal ? this.repo : this.remoteRepoUrl();
  }

  public String tagPath(DockerTag tag) {
    return defaultTagPath() + ":" + tag.tag;
  }

  public boolean hasDockerFile(Project project) {
    return new File(project.getProjectDir(), this.dockerFile).exists();
  }

  private String remoteRepoUrl() {
    String s = this.host + "/";
    if (this.org.isPresent()) {
      s += org.get() + '/';
    }
    s += this.repo;
    return s;
  }

  public static final class Builder {
    public boolean enabled = true;
    private String host;
    private String repo;
    private String org;
    private Boolean isLocal = false;
    private Boolean isHub = false;
    private String username;
    private String apiToken;
    private String dockerFile;
    private String buildDir;
    private List<DockerTag> tags = Collections.emptyList();
    private String versionString;
    private String dateStamp;

    public Builder() {
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder repo(String repo) {
      this.repo = repo;
      return this;
    }

    public Builder org(String org) {
      this.org = org;
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

    public Builder dockerFile(String dockerFile) {
      this.dockerFile = dockerFile;
      return this;
    }

    public Builder buildDir(String buildDir) {
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

    public Builder from(DockerInfo copy) {
      host = copy.host;
      repo = copy.repo;
      org = copy.org.orElse(null);
      isLocal = copy.isLocal;
      isHub = copy.isHub;
      username = copy.username;
      apiToken = copy.apiToken;
      dockerFile = copy.dockerFile;
      buildDir = copy.buildDir;
      tags = copy.tags;
      versionString = copy.versionString;
      dateStamp = copy.dateStamp;
      return this;
    }

    public Builder copy() {
      Builder copy = new Builder();
      copy.host(this.host);
      copy.repo(this.repo);
      copy.org(this.org);
      copy.isLocal(this.isLocal);
      copy.isHub(this.isHub);
      copy.username(this.username);
      copy.apiToken(this.apiToken);
      copy.dockerFile(this.dockerFile);
      copy.buildDir(this.buildDir);
      copy.tags(this.tags);
      copy.versionString(this.versionString);
      copy.dateStamp(this.dateStamp);
      return copy;
    }

    public DockerInfo build() {
      return new DockerInfo(this);
    }
  }
}
