package com.fetherbrik.gradle.afb.service;

import com.fetherbrik.gradle.afb.AnotherFineBuildExtension;
import com.fetherbrik.gradle.afb.domain.BuildInfo;
import com.fetherbrik.gradle.afb.domain.GitInfo;
import com.fetherbrik.gradle.afb.domain.configuration.DockerConfig;
import com.fetherbrik.gradle.afb.domain.configuration.ReleaseTarget;
import com.fetherbrik.gradle.afb.domain.DockerInfo;
import com.fetherbrik.gradle.afb.domain.DockerTag;
import com.fetherbrik.gradle.afb.domain.VersionInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gradle.api.Project;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildInfoTransform {

  private final AnotherFineBuildExtension extension;
  private final GitInfo git;
  // see semver.org and https://regex101.com/r/vkijKf/1/
  public static final String SEMVER_REGEX =
    "^([=v]?)(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
  public static final Pattern SEMVER_PATTERN = Pattern.compile(SEMVER_REGEX);

  public BuildInfoTransform(AnotherFineBuildExtension extension, GitInfo git) {
    this.extension = extension;
    this.git = git;
  } 

  public BuildInfo apply(Project project) {
    String dateStamp = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'z'"));
    VersionInfo info = getVersionInfo(git.versionString);
    ReleaseTarget target = getReleaseTarget(project, extension.getReleaseTargets(), info);
    DockerInfo dockerInfo = getDockerInfo(project, extension.getDocker(), target, info, dateStamp);
    return new BuildInfo.Builder()
      .versionInfoFilePath(extension.getVersionInfoFilePath())
      .dateStamp(dateStamp)
      .version(info)
      .target(target)
      .git(git)
      .docker(dockerInfo)
      .build();
  }

  private DockerInfo getDockerInfo(Project project, DockerConfig docker, ReleaseTarget target, VersionInfo version, String dateStamp) {
    DockerInfo result = DockerInfo.disabled();
    if(target.isDocker()) {
      String host = docker.getRepoHost();
      boolean isLocal = host.equals("");
      boolean isHub = host.equals("hub.docker.com");
      DockerInfo.Builder builder = new DockerInfo.Builder()
        .dateStamp(dateStamp)
        .versionString(version.full.toLowerCase())
        .isLocal(isLocal)
        .isHub(isHub)
        .host(host)
        .org(docker.getRepoOrg())
        .repo(docker.getRepoName().call(project))
        .buildDir("docker")
        .dockerFile("Dockerfile")
        .username(docker.getUsername())
        .apiToken(docker.getApiToken());
      if (target.getDockerTag() != null) {
        DockerTag placeholder = new DockerTag(target.getDockerTag(), "Tag image with '" + target.getDockerTag() + "'", target.getDockerTag());
        builder.tags(Lists.newArrayList(placeholder));
      }
      result = builder.build();
    }
    return result;
  }

  private ReleaseTarget getReleaseTarget(Project project, Map<String, ReleaseTarget> releaseTargets, VersionInfo versionInfo) {
    ReleaseTarget result = null;
    Optional<String> forceTargetTo = getForceTargetKey(project);
    if (forceTargetTo.isPresent()) {
      result = releaseTargets.get(forceTargetTo.get());
      if (result == null) {
        throw new RuntimeException("Cannot find target with key '" + forceTargetTo.get() + "'. Cannot force target to non-existing configuration.");
      }
    } else {
      for (Map.Entry<String, ReleaseTarget> entry : releaseTargets.entrySet()) {
        if (entry.getValue().matches(extension.getBuildType(), versionInfo.full)) {
          result = entry.getValue();
          break;
        }
      }
    }
    if (result == null) {
      throw new RuntimeException("Could not find matching release target for version '" + versionInfo.full + "'.");
    }
    return result;
  }

  private Optional<String> getForceTargetKey(Project project) {
    Optional<String> result = Optional.empty();
    if (project.hasProperty("forceTarget")) {
      result = Optional.of((String) project.getProperties().get("forceTarget"));
    } else if (System.getProperties().containsKey("forceTarget")) {
      result = Optional.of(System.getProperty("forceTarget"));
    }
    return result;
  }

  private VersionInfo getVersionInfo(String version) {
    Matcher matcher = SEMVER_PATTERN.matcher(version);
    if (!matcher.matches()) {
      throw new RuntimeException("Could not parse version from value provided by Git describe: '" + version + "': Pattern does not match '" + SEMVER_REGEX
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
    /* Mostly creating all these variables to get line numbers if there's an exception thrown. */
    String prefix = matcher.group(1);
    int major = Integer.parseInt(matcher.group(2));
    int minor = Integer.parseInt(matcher.group(3));
    int patch = Integer.parseInt(matcher.group(4));
    VersionInfo.Builder info = new VersionInfo.Builder();
    if(StringUtils.isNotBlank(prefix)){
      info.prefix(prefix);
    }
    info.major(major).minor(minor).patch(patch);
    if (matcher.groupCount() > 5) {
      String group = matcher.group(5);
      if(StringUtils.isNotBlank(group)) {
        info.preRelease(group);
      }
    }
    if (matcher.groupCount() > 6) {
      String group = matcher.group(6);
      if(StringUtils.isNotBlank(group)) {
        info.meta(group);
      }
    }
    return info.build();
  }
}
