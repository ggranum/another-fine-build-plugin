package com.geoffgranum.gradle.afb.service;

import com.geoffgranum.gradle.afb.AnotherFineBuildExtension;
import com.geoffgranum.gradle.afb.domain.BuildInfo;
import com.geoffgranum.gradle.afb.domain.DockerInfo;
import com.geoffgranum.gradle.afb.domain.DockerTag;
import com.geoffgranum.gradle.afb.domain.GitInfo;
import com.geoffgranum.gradle.afb.domain.VersionInfo;
import com.geoffgranum.gradle.afb.domain.configuration.DockerConfig;
import com.geoffgranum.gradle.afb.domain.configuration.ReleaseTarget;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.gradle.api.Project;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildInfoTransform {

    private final AnotherFineBuildExtension extension;
    private final GitInfo git;

    public BuildInfoTransform(AnotherFineBuildExtension extension, GitInfo git) {
        this.extension = extension;
        this.git = git;
    }

    public BuildInfo apply(Project project) {
        String dateStamp = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'z'"));
        Matcher matcher = getVersionMatcher(git.versionString);
        VersionInfo.Builder builder = new VersionInfo.Builder()
                .prefix("v")
                .major(Integer.parseInt(matcher.group(2)))
                .minor(Integer.parseInt(matcher.group(3)))
                .patch(Integer.parseInt(matcher.group(4)));
        if (matcher.groupCount() > 5) {
            builder.suffix(matcher.group(5));
        }
        VersionInfo versionInfo = builder.build();
        ReleaseTarget target = getReleaseTarget(extension.getReleaseTargets(), versionInfo);
        DockerInfo dockerInfo = getDockerInfo(project, extension.getDocker(), target, versionInfo, dateStamp);
        return new BuildInfo.Builder().dateStamp(dateStamp).version(versionInfo).target(target).git(git).docker(dockerInfo).build();
    }

    private DockerInfo getDockerInfo(Project project, DockerConfig docker, ReleaseTarget target, VersionInfo version, String dateStamp) {
        String host = docker.getRepoHost();
        boolean isLocal = host.equals("");
        boolean isHub = host.equals("hub.docker.com");
        DockerInfo.Builder builder = new DockerInfo.Builder()
                .dateStamp(dateStamp)
                .versionString(version.full.toLowerCase())
                .isLocal(isLocal)
                .isHub(isHub)
                .host(host)
                .repo(docker.getRepoName())
                .buildDir(new File(project.getBuildDir(), "/docker"))
                .dockerFile(new File(project.getProjectDir(), "Dockerfile"))
                .username(docker.getUsername())
                .apiToken(docker.getApiToken());
        if (target.getDockerTag() != null) {
            DockerTag placeholder = new DockerTag("placeholder", "Tag image with 'placeholder'", target.getDockerTag());
            builder.tags(Lists.newArrayList(placeholder));
        }
        return builder.build();
    }

    private ReleaseTarget getReleaseTarget(Map<String, ReleaseTarget> releaseTargets, VersionInfo versionInfo) {
        for (Map.Entry<String, ReleaseTarget> entry : releaseTargets.entrySet()) {
            if (entry.getValue().matches(extension.getBuildType(), versionInfo.full)) {
                return entry.getValue();
            }
        }
        throw new RuntimeException("Could not find matching release target for version '" + versionInfo.full + "'.");
    }

    private Matcher getVersionMatcher(String version) {
        String pattern = "(\\p{Alpha}*)(\\d}*).(\\d*).(\\d*)-?([\\p{Alnum}]*)";
        Matcher matcher = Pattern.compile(pattern).matcher(version);
        if (!matcher.matches()) {
            throw new RuntimeException("Could not parse version from value provided by Git describe: '"
                                       + version
                                       + "': Pattern does not match '"
                                       + pattern
                                       + "'.");
        } else if (matcher.groupCount() < 5) { // don't forget group 0 is always the whole match.
            throw new RuntimeException("Could not parse version from value provided by Git describe: '"
                                       + version
                                       + "': Too few match groups ("
                                       + matcher.groupCount()
                                       + ").");
        } else if (matcher.groupCount() > 6) {
            throw new RuntimeException("Could not parse version from value provided by Git describe: '"
                                       + version
                                       + "': Too many match groups ("
                                       + matcher.groupCount()
                                       + ").");
        } else if (!NumberUtils.isCreatable(matcher.group(2)) || !NumberUtils.isCreatable(matcher.group(3)) || !NumberUtils.isCreatable(matcher.group(4))) {
            throw new RuntimeException(String.format(
                    "Could not parse version from value provided by Git describe: '%s': Major ('%s'), minor ('%s') and patch ('%s') values must be integral values.",
                    version,
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4)));
        }
        return matcher;
    }
}
