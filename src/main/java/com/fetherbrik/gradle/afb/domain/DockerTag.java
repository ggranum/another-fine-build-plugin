package com.fetherbrik.gradle.afb.domain;

import org.apache.commons.lang3.StringUtils;

public class DockerTag {
  public final String shortName;
  public final String description;
  public final String tag;

  public DockerTag(String shortName, String description, String tag) {
    this.shortName = shortName;
    this.description = description;
    this.tag = tag;
  }

  public String capitalizedShortName() {
    return StringUtils.capitalize(shortName);
  }
}
