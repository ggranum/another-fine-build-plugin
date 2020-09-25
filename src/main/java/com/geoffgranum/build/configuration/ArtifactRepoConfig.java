package com.geoffgranum.build.configuration;

import groovy.lang.Closure;

public class ArtifactRepoConfig {
    private String groupId;
    private ArtifactRepoTarget deploy;
    private ArtifactRepoTarget read;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ArtifactRepoTarget getDeploy() {
        return deploy;
    }

    public void setDeploy(Closure<ArtifactRepoTarget> closure) {
        ArtifactRepoTarget t = new ArtifactRepoTarget();
        closure.setDelegate(t);
        closure.run();
        this.deploy = t;
    }

    public ArtifactRepoTarget getRead() {
        return read;
    }


    public void setRead(Closure<ArtifactRepoTarget> closure){
        ArtifactRepoTarget t = new ArtifactRepoTarget();
        closure.setDelegate(t);
        closure.run();
        this.deploy = t;
    }
}
