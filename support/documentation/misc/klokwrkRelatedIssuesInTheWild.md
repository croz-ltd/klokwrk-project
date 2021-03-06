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
* VERIFIED (4.5.1) - https://github.com/AxonFramework/AxonFramework/issues/1805

### Axon Server:
* VERIFIED (in Axon Framework 4.4.1) - https://github.com/AxonIQ/axon-server-se/issues/148

### Axon Tracing Extension
* VERIFIED (4.4) - https://github.com/AxonFramework/extension-tracing/issues/53

### CodeNarc
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/490
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/492
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/493
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/506
* VERIFIED (2.0) - https://github.com/CodeNarc/CodeNarc/issues/510
  * https://github.com/CodeNarc/CodeNarc/pull/536
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/511
* VERIFIED (2.0) - https://github.com/CodeNarc/CodeNarc/issues/512
* VERIFIED (1.6) - https://github.com/CodeNarc/CodeNarc/issues/516
* VERIFIED (next release) - https://github.com/CodeNarc/CodeNarc/issues/611
  * https://github.com/CodeNarc/CodeNarc/pull/635
* REPORTED - https://github.com/CodeNarc/CodeNarc/issues/612
* VERIFIED (next release)- https://github.com/CodeNarc/CodeNarc/issues/613
* VERIFIED (next release) - https://github.com/CodeNarc/CodeNarc/issues/614
  * https://github.com/CodeNarc/CodeNarc/pull/634
* VERIFIED (next release) - https://github.com/CodeNarc/CodeNarc/issues/632
  * https://github.com/CodeNarc/CodeNarc/pull/633
* VERIFIED (next release) - https://github.com/CodeNarc/CodeNarc/issues/636

### GitHub Action - gradle-command-action
* IN PROGRESS - https://github.com/eskatos/gradle-command-action/issues/22

### Gradle Dependency Analyze
* IN PROGRESS - https://github.com/gradle-dependency-analyze/gradle-dependency-analyze/issues/108

### Groovy
* VERIFIED (3.0.4) - https://issues.apache.org/jira/browse/GROOVY-9546
* VERIFIED (3.0.4) - https://issues.apache.org/jira/browse/GROOVY-9547
* VERIFIED (3.0.5) - https://issues.apache.org/jira/browse/GROOVY-9577
* VERIFIED (3.0.5) - https://issues.apache.org/jira/browse/GROOVY-9643
* VERIFIED (3.0.7) - https://issues.apache.org/jira/browse/GROOVY-9770
* VERIFIED (3.0.7) - https://issues.apache.org/jira/browse/GROOVY-9772
* VERIFIED (3.0.8) - https://issues.apache.org/jira/browse/GROOVY-9858
  * https://groovy.markmail.org/thread/gmcivt4ywcntszwx
  * https://github.com/apache/groovy/pull/1448
* FIXED (4.0.0) - https://issues.apache.org/jira/browse/GROOVY-10052
* IN PROGRESS - https://issues.apache.org/jira/browse/GROOVY-10055
* IN PROGRESS - https://issues.apache.org/jira/browse/GROOVY-10156

### IDEA
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-216308
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-239626
* VERIFIED (2020.3) - https://youtrack.jetbrains.com/issue/IDEA-239966
* IN PROGRESS - https://youtrack.jetbrains.com/issue/IDEA-243620
* VERIFIED (2021.1) - https://youtrack.jetbrains.com/issue/IDEA-255422
* IN PROGRESS - https://youtrack.jetbrains.com/issue/IDEA-261239
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-270964

### JReleaser
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/85
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/86
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/98
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/99
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/100
* FIXED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/114
* RESOLVED (0.3.0)- https://github.com/jreleaser/jreleaser/issues/120
* FIXED (0.4.0) - https://github.com/jreleaser/jreleaser/issues/121

### Kordamp Gradle Plugins
* VERIFIED (0.36.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/291
* VERIFIED (0.36.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/297
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/301
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/304
* VERIFIED (0.37.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/328
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/341
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/345
* VERIFIED (0.41.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/381
* INVALID - https://github.com/kordamp/kordamp-gradle-plugins/issues/382
* RESOLVED - https://github.com/kordamp/kordamp-gradle-plugins/issues/415
* VERIFIED (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/417
* VERIFIED (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/420
* VERIFIED (0.42.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/421
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/469
* IN PROGRESS - https://github.com/kordamp/kordamp-gradle-plugins/issues/486

### micronaut-core
* IN PROGRESS - https://github.com/micronaut-projects/micronaut-core/issues/4622

### micronaut-gradle-plugin
* REPORTED - https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/92
* REPORTED - https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/93

### Spock Framework
* VERIFIED (in GROOVY-9643) - https://github.com/spockframework/spock/issues/1177

### Spring Boot
* VERIFIED (2.3.2.RELEASE) - https://github.com/spring-projects/spring-boot/issues/22200
* VERIFIED - https://github.com/spring-projects/spring-boot/issues/26774

### Spring Boot Wavefront extension
* VERIFIED (2.0.0) - https://github.com/wavefrontHQ/wavefront-spring-boot/issues/57

## List of monitored issues
This is a list of all issues that are NOT reported by `klokwrk-project` team members, but are relevant for the project. Those issues are monitored by team members.

When particular issue is resolved, it can be removed from this list when there is no value to monitor it further.

### Codenarc
* [MissingBlankLineBeforeAnnotatedField - False-positive at the beginning of a class](https://github.com/CodeNarc/CodeNarc/issues/606)

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
