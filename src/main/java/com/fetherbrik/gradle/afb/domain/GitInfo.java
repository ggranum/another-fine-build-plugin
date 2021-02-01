package com.fetherbrik.gradle.afb.domain;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitInfo {

  /**
   * The long describe: 'git describe --long --always'
   */
  public final String gitRoot;
  public final String describe;
  public final String branchName;
  public final String hash;
  public final boolean isDirty;
  public final int distanceToLastTag;
  public final String versionString;

  private GitInfo(Builder builder) {
    gitRoot = builder.gitRoot;
    describe = builder.describe;
    branchName = builder.branchName;
    hash = builder.hash;
    isDirty = builder.isDirty;
    distanceToLastTag = distanceToLastTagFromDescribe(describe);
    versionString = determineVersionString(describe, hash);
  }

  public String shortHash() {
    return hash.substring(0, 7);
  }

  private String determineVersionString(String describe, String hash) {
    String result = "";
    Matcher matcher = Pattern.compile("(v.*)-([\\d]*)-[\\p{Alnum}]{8}").matcher(describe);
    if (matcher.matches()) {
      result = matcher.group(1);
      System.out.println("AFB: Found version tag '" + result + "'");
    } else if (describe.endsWith(hash.substring(0, 7))) {
      result = "v0.0.0-NOTAG";
      System.out.println("AFB: No Version tag found, using default version of '" + result + "'");

    }
    return result;
  }

  private int distanceToLastTagFromDescribe(String describe) {
    int result = -1;
    Matcher matcher = Pattern.compile(".*-([\\d]*)-[\\p{Alnum}]{8}").matcher(describe);
    if (matcher.matches()) {
      String value = matcher.group(1);
      try {
        result = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        System.err.println("Could not parse distance from git describe string '" + describe + "'");
      }
    }
    return result;
  }

  public static final class Builder {
    private String gitRoot;
    private String describe;
    private String branchName;
    private String hash;
    private Boolean isDirty = false;
    private Integer distanceToLastTag = 0;
    private String versionString;

    public Builder() {
    }

    public Builder gitRoot(String gitRoot) {
      File f = new File(gitRoot);
      if (!f.exists()) {
        throw new RuntimeException("'" + gitRoot + "' is not a valid Git root path.");
      }
      try {
        this.gitRoot = f.getCanonicalPath();
      } catch (IOException e) {
        throw new RuntimeException("Could not determine canonical path for gitRoot '" + f.getAbsolutePath() + "'");
      }
      return this;
    }

    public Builder describe(String describe) {
      this.describe = describe;
      return this;
    }

    public Builder branchName(String branchName) {
      this.branchName = branchName;
      return this;
    }

    public Builder hash(String hash) {
      this.hash = hash;
      return this;
    }

    public Builder isDirty(boolean isDirty) {
      this.isDirty = isDirty;
      return this;
    }

    public Builder distanceToLastTag(int distanceToLastTag) {
      this.distanceToLastTag = distanceToLastTag;
      return this;
    }

    public Builder versionString(String versionString) {
      this.versionString = versionString;
      return this;
    }

    public Builder from(GitInfo copy) {
      gitRoot = copy.gitRoot;
      describe = copy.describe;
      branchName = copy.branchName;
      hash = copy.hash;
      isDirty = copy.isDirty;
      distanceToLastTag = copy.distanceToLastTag;
      versionString = copy.versionString;
      return this;
    }

    public Builder copy() {
      Builder copy = new Builder();
      copy.gitRoot(this.gitRoot);
      copy.describe(this.describe);
      copy.branchName(this.branchName);
      copy.hash(this.hash);
      copy.isDirty(this.isDirty);
      copy.distanceToLastTag(this.distanceToLastTag);
      copy.versionString(this.versionString);
      return copy;
    }

    public GitInfo build() {
      return new GitInfo(this);
    }
  }
}
