package com.geoffgranum.gradle.afb.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitInfo {

    /**
     * The long describe: 'git describe --long --always'
     */
    public final String describe;
    public final String branchName;
    public final String hash;
    public final boolean isDirty;
    public final int distanceToLastTag;
    public final String versionString;

    private GitInfo(Builder builder) {
        describe = builder.describe;
        branchName = builder.branchName;
        hash = builder.hash;
        isDirty = builder.isDirty;
        distanceToLastTag = distanceToLastTagFromDescribe(describe);
        versionString = determineVersionString(describe);
    }

    public String shortHash() {
        return hash.substring(0, 8);
    }

    private String determineVersionString(String describe) {
        String result = "";
        Matcher matcher = Pattern.compile("(v.*)-([\\d]*)-[\\p{Alnum}]{8}").matcher(describe);
        if (matcher.matches()) {
            result = matcher.group(1);
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

        private String describe;
        private String branchName;
        private String hash;

        private Boolean isDirty = false;

        public Builder() {
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

        public GitInfo build() {
            return new GitInfo(this);
        }
    }
}
