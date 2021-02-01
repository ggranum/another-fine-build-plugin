package com.fetherbrik.gradle.afb.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version according to https://github.com/npm/node-semver#functions, but specific to enabling NPM-like `version {target}`
 * functionality.
 * <p>
 * For example:
 * v1.2.3-RC.4
 * v1.2.3+103
 * v1.2.3+20210130T0830
 * v1.2.3-DEV+20210130T0830
 * <p>
 * While Semver allows for an arbitrary list of dot-separated pre-release values, `npm version prerelease` just adds a
 * counter at the end of the pre-release chain.
 */
public final class VersionInfo {
  public static final String PRE_RELEASE_OR_META_REGEX = "^(.*?[\\W])(\\d+)$";
  public static final Pattern PRE_RELEASE_OR_META_PATTERN = Pattern.compile(PRE_RELEASE_OR_META_REGEX);
  public final String full;
  public final String maven;
  public final int major;
  public final int minor;
  public final int patch;
  public final Optional<String> preRelease;
  public final Optional<String> meta;

  private VersionInfo(Builder builder) {
    major = builder.major;
    minor = builder.minor;
    patch = builder.patch;
    preRelease = Optional.ofNullable(builder.preRelease);
    meta = Optional.ofNullable(builder.meta);
    StringBuilder sb = new StringBuilder(String.format("%s.%s.%s", major, minor, patch));
    preRelease.ifPresent(s -> {
      if (!s.isBlank()) {
        sb.append("-").append(s);
      }
    });
    meta.ifPresent(s -> {
      if (!s.isBlank()) {
        sb.append("-").append(s);
      }
    });
    maven = sb.toString();
    full = "v" + sb.toString();
  }

  public Builder copy() {
    return new Builder().from(this);
  }

  public VersionInfo nextPatch() {
    return new Builder().from(this).patch(patch + 1).build();
  }

  public VersionInfo nextMinor() {
    return new Builder().from(this).minor(minor + 1).patch(0).build();
  }

  public VersionInfo nextMajor() {
    return new Builder().from(this).major(major + 1).minor(0).patch(0).build();
  }

  public Builder nextPreRelease() {
    String releaseText = "";
    int nextCount = 0;
    if (preRelease.isPresent()) {
      Pair<Optional<String>, Optional<Integer>> preReleasePair = getPreReleaseOrMetaPair(preRelease.get());
      if (preReleasePair.getLeft().isPresent()) {
        releaseText = preReleasePair.getLeft().get();
        if (!releaseText.matches("[\\w]+[\\W]$")) {
          releaseText = releaseText + ".";
        }
      }
      if (preReleasePair.getRight().isPresent()) {
        nextCount = preReleasePair.getRight().get() + 1;
      }
    }
    return this.copy().preRelease(releaseText + nextCount);
  }

  private Pair<Optional<String>, Optional<Integer>> getPreReleaseOrMetaPair(String text) {
    Matcher matcher = PRE_RELEASE_OR_META_PATTERN.matcher(text);
    Optional<String> body = Optional.empty();
    Optional<Integer> counter = Optional.empty();
    if (matcher.matches()) {
      String value = matcher.group(1);
      if (StringUtils.isNotBlank(value)) {
        body = Optional.of(value);
      }
      value = matcher.groupCount() > 2 ? matcher.group(2) : null;
      if (StringUtils.isNotBlank(value)) {
        counter = Optional.of(Integer.parseInt(value));
      }
    }
    return Pair.of(body, counter);
  }

  private boolean hasPreRelease() {
    return preRelease.isPresent() && StringUtils.isNotBlank(preRelease.get());
  }

  public static final class Builder {
    private Integer major = 0;
    private Integer minor = 0;
    private Integer patch = 0;
    private String prefix;
    private String preRelease;
    private String meta;

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

    public Builder preRelease(String suffix) {
      this.preRelease = suffix;
      return this;
    }

    public Builder from(VersionInfo copy) {
      major = copy.major;
      minor = copy.minor;
      patch = copy.patch;
      prefix = copy.preRelease.orElse(null);
      preRelease = copy.preRelease.orElse(null);
      return this;
    }

    public Builder copy() {
      Builder copy = new Builder();
      copy.major(this.major);
      copy.minor(this.minor);
      copy.patch(this.patch);
      copy.prefix(this.prefix);
      copy.preRelease(this.preRelease);
      copy.preRelease(this.preRelease);
      return copy;
    }

    public VersionInfo build() {
      averPrefixValid();
      return new VersionInfo(this);
    }

    public void meta(String meta) {
      this.meta = meta;
    }

    private void averPrefixValid() {
      if (prefix != null) {
        if (!prefix.toLowerCase().equals("v") && !prefix.equals("=")) {
          throw new IllegalArgumentException(String.format("Semver prefix may only be 'v' or '='. Found '%s'", prefix));
        }
      }
    }
  }
}

