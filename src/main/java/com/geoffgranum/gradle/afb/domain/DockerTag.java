package com.geoffgranum.gradle.afb.domain;

public class DockerTag {
    public final String shortName;
    public final String description;
    public final String tag;

    public DockerTag(String shortName, String description, String tag) {
        this.shortName = shortName;
        this.description = description;
        this.tag = tag;
    }
}
