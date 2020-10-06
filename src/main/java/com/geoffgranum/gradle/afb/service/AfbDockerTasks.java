package com.geoffgranum.gradle.afb.service;

import com.geoffgranum.gradle.afb.AnotherFineBuildPlugin;
import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.DockerInfo;
import com.geoffgranum.gradle.afb.domain.DockerTag;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AfbDockerTasks {
    public void applyDockerTasks(Project project, BuildInfo info) {
        if (new File(project.getProjectDir(), info.docker.dockerFile).exists()) {
            Copy dockerAssemble = addDockerAssembleTask(project, info);
            Exec dockerBuild = addDockerBuildTask(project, dockerAssemble, info);
            Task dockerApplyAllTags = addDockerAllTagsTask(project, dockerBuild, info);
            List<Exec> dockerTags = addDockerTagTasks(project, dockerApplyAllTags, info);
            Optional<Task> dockerLogin = addDockerLoginTask(project, info);
            Exec dockerPush = addDockerPushTask(project, dockerLogin, dockerApplyAllTags, info);
        }
    }

    private Copy addDockerAssembleTask(Project project, BuildInfo info) {
        DockerInfo docker = info.docker;
        return project.getTasks().create("dockerAssemble", Copy.class, assembleTask -> {
            assembleTask.setGroup(AnotherFineBuildPlugin.GROUP);
            assembleTask.setDescription("Assemble docker content for build.");
            assembleTask.from(docker.dockerFile);
            assembleTask.into(new File(project.getBuildDir(), docker.buildDir));
            assembleTask.with(project.copySpec());
        });
    }

    private Exec addDockerBuildTask(Project project, Copy assembleTask, BuildInfo info) {
        DockerInfo docker = info.docker;
        return project.getTasks().create("dockerBuild", Exec.class, buildTask -> {
            buildTask.setGroup(AnotherFineBuildPlugin.GROUP);
            buildTask.setDescription("Build the primary docker image");
            buildTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
            buildTask.setCommandLine("docker", "build", "--tag", docker.baseRepoTag(), ".");
            buildTask.dependsOn(assembleTask);
        });
    }

    private Exec addDockerPushTask(Project project, Optional<Task> dockerLogin, Task applyAllTagsTask, BuildInfo info) {
        DockerInfo docker = info.docker;
        return project.getTasks().create("dockerPush", Exec.class, pushTask -> {
            pushTask.setGroup(AnotherFineBuildPlugin.GROUP);
            pushTask.setDescription("Push the image to the configured docker host.");
            pushTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
            pushTask.setCommandLine("docker", "push", docker.baseRepoTag());
            pushTask.dependsOn(applyAllTagsTask);
            if (dockerLogin.isPresent()) {
                pushTask.dependsOn(dockerLogin);
            }
        });
    }

    /**
     * Create a single task that we can target for all the tags on the image. This task will 'dependOn' each child task.
     */
    private Task addDockerAllTagsTask(Project project, Exec buildTask, BuildInfo info) {
        return project.getTasks().create("dockerApplyAllTags", Task.class, allTagsTask -> {
            allTagsTask.setGroup(AnotherFineBuildPlugin.GROUP);
            allTagsTask.setDescription("Apply all configured tag tasks.");
            allTagsTask.dependsOn(buildTask);
        });
    }

    private List<Exec> addDockerTagTasks(Project project, Task dockerApplyAllTags, BuildInfo info) {
        List<Exec> result = new ArrayList<>();
        DockerInfo docker = info.docker;
        for (DockerTag tag : docker.tags) {
            Exec task = project.getTasks().create("dockerTag" + tag.shortName, Exec.class, (tagTask -> {
                tagTask.setGroup(AnotherFineBuildPlugin.GROUP);
                tagTask.setDescription(tag.description);
                tagTask.setWorkingDir(new File(project.getBuildDir(), docker.buildDir));
                tagTask.setCommandLine("docker", "tag", docker.baseRepoTag(), tag.tag);
                dockerApplyAllTags.dependsOn(tagTask);
            }));
            result.add(task);
        }
        return result;
    }

    private Optional<Task> addDockerLoginTask(Project project, BuildInfo info) {
        Optional<Task> result = Optional.empty();
        if (!info.docker.isLocal) {
            DockerInfo docker = info.docker;
            Task dockerLogin = project.getTasks().create("dockerLogin", Task.class, task -> {
                task.setGroup(AnotherFineBuildPlugin.GROUP);
                task.setDescription("Login to docker");
                task.doLast(t -> {
                    if (!project.getRootProject().getExtensions().getExtraProperties().has("afb.dockerLoginHasRun")) {
                        project.exec(execSpec -> {
                            execSpec.executable("docker");
                            if (docker.host.contains("hub.docker.com")) {
                                execSpec.args("login", "-u", docker.username, "-p", docker.apiToken);
                            } else {
                                execSpec.args("login", "-u", docker.username, "-p", docker.apiToken, docker.host);
                            }
                        });
                        project.getRootProject().getExtensions().getExtraProperties().set("afb.dockerLoginHasRun", true);
                    }
                });
            });
            result = Optional.of(dockerLogin);
        }
        return result;
    }
}
