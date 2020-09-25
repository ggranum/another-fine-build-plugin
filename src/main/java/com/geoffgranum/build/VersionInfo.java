package com.geoffgranum.build;

public final class VersionInfo {
    public final String major;
    public final String minor;
    public final String patch;
    public final String suffix;
    public final boolean isDirty;
    public final int distanceToLastTag;


    private VersionInfo(Builder builder) {
        major = builder.major;
        minor = builder.minor;
        patch = builder.patch;
        suffix = builder.suffix;
        isDirty = builder.isDirty;
        distanceToLastTag = builder.distanceToLastTag;
    }

    public static final class Builder {
        private String major;
        private String minor;
        private String patch;
        private String suffix;
        private Boolean isDirty = false;
        private Integer distanceToLastTag = 0;

        public Builder() {
        }

        public Builder major(String major) {
            this.major = major;
            return this;
        }

        public Builder minor(String minor) {
            this.minor = minor;
            return this;
        }

        public Builder patch(String patch) {
            this.patch = patch;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
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

        public VersionInfo build() {
            return new VersionInfo(this);
        }
    }
}

