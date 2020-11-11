# Klokwrk Related Issues in the Wild
Here we maintain list of issues in 3rd party software that are discovered during `klokwrk-project` development.

## Statuses
| Status              | Description |
|:------------------- |:---|
| REPORTED            | Issue is reported but without any response. |
| IN PROGRESS         | Issue is reported, and some activity happened. |
| FIXED               | Issue is fixed and is waiting to be published as part of official release. |
| RELEASED            | Issue is fixed and released, but not yet applied in `klokwrk-project`. |
| VERIFIED            | After release (or after our own jitpack release), we tried it and it works for `klokwkr-project`. |
| VERIFICATION FAILED | After release, we did try it and it does not work for `klokwrk-project`. |
| WON'T FIX           | Issue is declined by a maintainer. |
| INVALID             | Issue is reported, but is resolved as invalid. |
| RESOLVED            | Issue is reported, and can be resolved without any fix. Similar to question. |

## List of reported issues
This is a list of all issues, categorized per 3rd party software, that are reported by `klokwrk-project` team members.

### Axon Framework
* VERIFIED (4.3.2) - https://github.com/AxonFramework/AxonFramework/issues/1407
* VERIFIED (4.4.0) - https://github.com/AxonFramework/AxonFramework/pull/1461
  * https://github.com/AxonFramework/AxonFramework/pull/1371#issuecomment-648673152
* VERIFIED (4.4.2) - https://github.com/AxonFramework/AxonFramework/issues/1481

### Axon Server:
* VERIFIED (in Axon Framework 4.4.1) - https://github.com/AxonIQ/axon-server-se/issues/148

### Axon Tracing Extension
* VERIFIED (4.4) - https://github.com/AxonFramework/extension-tracing/issues/53

### CodeNarc
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/490
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/492
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/493
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/506
* VERIFIED (2.0 jitpack - commit 8e3f130a53 of 2020-08-02) - https://github.com/CodeNarc/CodeNarc/issues/510
  * https://github.com/CodeNarc/CodeNarc/pull/536
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/511
* VERIFIED (2.0 jitpack - commit 28ad028e05 of 2020-07-24) - https://github.com/CodeNarc/CodeNarc/issues/512
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/516

### GitHub Action - gradle-command-action
* IN PROGRESS - https://github.com/eskatos/gradle-command-action/issues/22

### Gradle Dependency Analyze
* IN PROGRESS - https://github.com/wfhartford/gradle-dependency-analyze/issues/108

### Groovy
* VERIFIED (3.0.4) - https://issues.apache.org/jira/browse/GROOVY-9546
* VERIFIED (3.0.4) - https://issues.apache.org/jira/browse/GROOVY-9547
* VERIFIED (3.0.5) - https://issues.apache.org/jira/browse/GROOVY-9577
* VERIFIED (3.0.5) - https://issues.apache.org/jira/browse/GROOVY-9643
* FIXED (3.0.7) - https://issues.apache.org/jira/browse/GROOVY-9770
* REPORTED - https://issues.apache.org/jira/browse/GROOVY-9772

### IDEA
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-216308
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-239626
* FIXED (2020.3) - https://youtrack.jetbrains.com/issue/IDEA-239966
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-243620

### Kordamp Gradle Plugins
* VERIFIED (0.36.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/291
* VERIFIED (0.36.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/297
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/301
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/304
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/328
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/341
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/345
* RELEASED (0.41.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/381
* INVALID - https://github.com/kordamp/kordamp-gradle-plugins/issues/382
* RESOLVED - https://github.com/kordamp/kordamp-gradle-plugins/issues/415
* IN PROGRESS (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/417
* IN PROGRESS (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/420
* IN PROGRESS (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/421

### Spock Framework
* VERIFIED (in GROOVY-9643) - https://github.com/spockframework/spock/issues/1177

### Spring Boot
* VERIFIED (2.3.2.RELEASE) - https://github.com/spring-projects/spring-boot/issues/22200

### Spring Boot Wavefront extension
* VERIFIED (2.0.0) - https://github.com/wavefrontHQ/wavefront-spring-boot/issues/57

## List of monitored issues
This is a list of all issues that are NOT reported by `klokwrk-project` team members, but are relevant for the project. Those issues are monitored by team members.

When particular issue is resolved, it can be removed from this list when there is no value to monitor it further.

### Github
* [GitHub IS 1017 - Support semi-linear merge option for pull requests](https://github.com/isaacs/github/issues/1017)
  * related to [ADR 0007 - ADR-0007 - Git Workflow with Linear History](../adr/content/0007-git-workflow-with-linear-history.md)
* [GitHub IS 1143 - Rebase and merge pull request option should add a merge commit](https://github.com/isaacs/github/issues/1143)
  * related to [ADR 0007 - ADR-0007 - Git Workflow with Linear History](../adr/content/0007-git-workflow-with-linear-history.md)

### Groovy
* [Groovy IS 9373 - ASM: rework line numbers...](https://issues.apache.org/jira/browse/GROOVY-9373)

### JaCoCo
* [JaCoCo PR 321 - Exceptions cause missed branches in previous lines](https://github.com/jacoco/jacoco/pull/321)
* [JaCoCo IS 884 - Groovy 2.5.5 unreachable line in byte code](https://github.com/jacoco/jacoco/issues/884)

### license-gradle-plugin (https://github.com/hierynomus/license-gradle-plugin)
* [Gradle 6 compatibility - missing task annotations #179](https://github.com/hierynomus/license-gradle-plugin/issues/179)
  * When resolved, licensing in main build.gradle can be turned on.
