package com.fetherbrik.gradle.afb.domain;

import java.util.Optional;

public final class VersionInfo {
  public final String full;
  public final String maven;

  public final int major;
  public final int minor;
  public final int patch;
  public final Optional<String> prefix;
  public final Optional<String> suffix;

  private VersionInfo(Builder builder) {
    major = builder.major;
    minor = builder.minor;
    patch = builder.patch;
    prefix = Optional.ofNullable(builder.prefix);
    suffix = Optional.ofNullable(builder.suffix);
    StringBuilder sb = new StringBuilder(String.format("%s.%s.%s", major, minor, patch));
    suffix.ifPresent(s -> sb.append("-").append(s));
    maven = sb.toString();

    full = sb.toString();
  }

  public VersionInfo.Builder copy() {
    return new Builder().from(this);
  }

  public VersionInfo nextPatch(String suffix) {
    return new Builder().from(this).patch(patch + 1).suffix(suffix).build();
  }

  public VersionInfo nextPatch() {
    return new Builder().from(this).patch(patch + 1).build();
  }

  public VersionInfo nextMinor(String suffix) {
    return new Builder().from(this).minor(minor + 1).patch(0).suffix(suffix).build();
  }
  public VersionInfo nextMinor() {
    return new Builder().from(this).minor(minor + 1).patch(0).build();
  }

  public VersionInfo nextMajor(String suffix) {
    return new Builder().from(this).major(major + 1).minor(0).patch(0).suffix(suffix).build();
  }

  public VersionInfo nextMajor() {
    return new Builder().from(this).major(major + 1).minor(0).patch(0).build();
  }



  public static final class Builder {
    private Integer major = 0;
    private Integer minor = 0;
    private Integer patch = 0;
    private String prefix;
    private String suffix;

    public Builder() {
    }

    public Builder major(int major) {
      this.major = major;
      return this;
    }

    public Builder minor(int minor) {
      this.minor = minor;
      return this;
    }

    public Builder patch(int patch) {
      this.patch = patch;
      return this;
    }

    public Builder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder suffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Builder from(VersionInfo copy) {
      major = copy.major;
      minor = copy.minor;
      patch = copy.patch;
      prefix = copy.prefix.orElse(null);
      suffix = copy.suffix.orElse(null);
      return this;
    }

    public Builder copy() {
      Builder copy = new Builder();
      copy.major(this.major);
      copy.minor(this.minor);
      copy.patch(this.patch);
      copy.prefix(this.prefix);
      copy.suffix(this.suffix);
      return copy;
    }

    public VersionInfo build() {
      return new VersionInfo(this);
    }
  }
}

