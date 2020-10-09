package com.fetherbrik.gradle.afb;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class AnotherFineBuildTask extends DefaultTask {
    private final Property<String> serverUrl;

    public AnotherFineBuildTask() {
        serverUrl = getProject().getObjects().property(String.class);
    }

    @Input
    public Property<String> getServerUrl() {
        return serverUrl;
    }

    @Override
    public Task configure(Closure closure) {
        return super.configure(closure);
    }

    @TaskAction
    public void resolveLatestVersion() {
        // Access the raw value during the execution phase of the build lifecycle
        System.out.println("Retrieving latest artifact version from URL " + serverUrl.get());

        // do additional work
    }
}
