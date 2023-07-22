# Klokwrk Related Issues in the Wild
Here we maintain list of issues in 3rd party software that are discovered during `klokwrk-project` development.

## Statuses
| Status              | Description                                                                                       |
|:--------------------|:--------------------------------------------------------------------------------------------------|
| REPORTED            | Issue is reported but without any response.                                                       |
| IN PROGRESS         | Issue is reported, and some activity happened.                                                    |
| FIXED               | Issue is fixed and is waiting to be published as part of official release.                        |
| RELEASED            | Issue is fixed and released, but not yet applied in `klokwrk-project`.                            |
| VERIFIED            | After release (or after our own jitpack release), we tried it and it works for `klokwkr-project`. |
| VERIFICATION FAILED | After release, we did try it and it does not work for `klokwrk-project`.                          |
| WON'T FIX           | Issue is declined by a maintainer.                                                                |
| INVALID             | Issue is reported, but is resolved as invalid.                                                    |
| RESOLVED            | Issue is reported, and can be resolved without any fix. Similar to question.                      |
| OBSOLETE            | Issue is reported and recognized, but will be resolved in some other way.                         |
| NOT A BUG           | Issue is reported, but resolved as not a bug.                                                     |

## List of reported issues
This is a list of all issues, categorized per 3rd party software, that are reported by `klokwrk-project` team members.

### Axon Framework
* VERIFIED (4.3.2) - https://github.com/AxonFramework/AxonFramework/issues/1407
* VERIFIED (4.4.0) - https://github.com/AxonFramework/AxonFramework/pull/1461
  * https://github.com/AxonFramework/AxonFramework/pull/1371#issuecomment-648673152
* VERIFIED (4.4.2) - https://github.com/AxonFramework/AxonFramework/issues/1481
* VERIFIED (4.5.1) - https://github.com/AxonFramework/AxonFramework/issues/1805
* VERIFIED (4.6.2) - https://github.com/AxonFramework/AxonFramework/issues/1901
  * https://github.com/AxonFramework/AxonFramework/pull/1905
* VERIFIED (4.6.0) - https://github.com/AxonFramework/AxonFramework/pull/1910
* VERIFIED (4.6.2) - https://github.com/AxonFramework/AxonFramework/issues/2454
* IN PROGRESS (4.9.0) - https://github.com/AxonFramework/AxonFramework/issues/2780

### Axon Framework - IDEA Plugin
* REPORTED - https://github.com/AxonFramework/IdeaPlugin/issues/64

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
* VERIFIED (2.2.0) - https://github.com/CodeNarc/CodeNarc/issues/611
  * https://github.com/CodeNarc/CodeNarc/pull/635
* WON'T FIX - https://github.com/CodeNarc/CodeNarc/issues/612
* VERIFIED (2.2.0)- https://github.com/CodeNarc/CodeNarc/issues/613
* VERIFIED (2.2.0) - https://github.com/CodeNarc/CodeNarc/issues/614
  * https://github.com/CodeNarc/CodeNarc/pull/634
* VERIFIED (2.2.0) - https://github.com/CodeNarc/CodeNarc/issues/632
  * https://github.com/CodeNarc/CodeNarc/pull/633
* VERIFIED (2.2.0) - https://github.com/CodeNarc/CodeNarc/issues/636
* VERIFIED (3.0.0) - https://github.com/CodeNarc/CodeNarc/issues/660
  * https://github.com/CodeNarc/CodeNarc/pull/661
* VERIFIED (3.0.0) - https://github.com/CodeNarc/CodeNarc/issues/662
  * https://github.com/CodeNarc/CodeNarc/pull/663

### GitHub Action - gradle-build-action
* VERIFIED - https://github.com/gradle/gradle-build-action/issues/22

### GraalVM - Native Build TOOLS
* FIXED (0.9.6) - https://github.com/graalvm/native-build-tools/issues/129

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
* VERIFIED (3.0.9) - https://issues.apache.org/jira/browse/GROOVY-10052
* VERIFIED (4.0.0) - https://issues.apache.org/jira/browse/GROOVY-10055
* IN PROGRESS - https://issues.apache.org/jira/browse/GROOVY-10156
* VERIFIED (3.0.10) - https://issues.apache.org/jira/browse/GROOVY-10318
* VERIFIED (4.0.7) - https://issues.apache.org/jira/browse/GROOVY-10815
* VERIFIED (4.0.7) - https://issues.apache.org/jira/browse/GROOVY-10878
* VERIFIED (4.0.8) - https://issues.apache.org/jira/browse/GROOVY-10882
* NOT A BUG - https://issues.apache.org/jira/browse/GROOVY-10899

### datasource-micrometer
* IN PROGRESS - https://github.com/jdbc-observations/datasource-micrometer/issues/17
  * https://github.com/jdbc-observations/datasource-micrometer/issues/21
  * https://github.com/micrometer-metrics/micrometer/pull/3867
* FIXED (1.0.3) - https://github.com/jdbc-observations/datasource-micrometer/issues/18
  * https://github.com/jdbc-observations/datasource-micrometer/pull/19*

### IDEA
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-216308
* VERIFIED (2022.1) - https://youtrack.jetbrains.com/issue/IDEA-239626
* VERIFIED (2020.3) - https://youtrack.jetbrains.com/issue/IDEA-239966
* IN PROGRESS - https://youtrack.jetbrains.com/issue/IDEA-243620
* VERIFIED (2021.1) - https://youtrack.jetbrains.com/issue/IDEA-255422
* VERIFIED (2021.2) - https://youtrack.jetbrains.com/issue/IDEA-261239
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-270964
* VERIFIED (2022.1) - https://youtrack.jetbrains.com/issue/IDEA-285153
* IN PROGRESS - https://youtrack.jetbrains.com/issue/IDEA-285736
* VERIFIED (2022.2) - https://youtrack.jetbrains.com/issue/IDEA-286813
* REPORTED - https://youtrack.jetbrains.com/issue/IDEA-292254
* VERIFIED - https://youtrack.jetbrains.com/issue/IDEA-295389
* IN PROGRESS - https://youtrack.jetbrains.com/issue/IDEA-315953

### JReleaser
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/85
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/86
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/98
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/99
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/100
* VERIFIED (0.3.0) - https://github.com/jreleaser/jreleaser/issues/114
* RESOLVED (0.3.0)- https://github.com/jreleaser/jreleaser/issues/120
* VERIFIED (0.4.0) - https://github.com/jreleaser/jreleaser/issues/121

### JReleaser - GitHub action release-action
* VERIFIED - https://github.com/jreleaser/release-action/issues/7
* VERIFIED - https://github.com/jreleaser/release-action/issues/9
* VERIFIED - https://github.com/jreleaser/release-action/issues/10

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
* OBSOLETE - https://github.com/kordamp/kordamp-gradle-plugins/issues/469
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/486
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/520
* INVALID - https://github.com/kordamp/kordamp-gradle-plugins/issues/521
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/522
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/523
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/524
* VERIFIED (0.50.0) - https://github.com/kordamp/kordamp-gradle-plugins/issues/525

### micronaut-core
* OBSOLETE - https://github.com/micronaut-projects/micronaut-core/issues/4622
* REPORTED - https://github.com/micronaut-projects/micronaut-core/issues/7220

### micronaut-gradle-plugin
* REPORTED - https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/92
* OBSOLETE - https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/93

### Spock Framework
* VERIFIED (in GROOVY-9643) - https://github.com/spockframework/spock/issues/1177

### Spring Boot
* VERIFIED (2.3.2.RELEASE) - https://github.com/spring-projects/spring-boot/issues/22200
* VERIFIED - https://github.com/spring-projects/spring-boot/issues/26774

### Spring Boot Wavefront extension
* VERIFIED (2.0.0) - https://github.com/wavefrontHQ/wavefront-spring-boot/issues/57

### uuid-creator
* VERIFIED (4.4.1) - https://github.com/f4b6a3/uuid-creator/issues/60
* VERIFIED (4.6.1) - https://github.com/f4b6a3/uuid-creator/issues/64

## List of monitored issues
This is a list of all issues that are NOT reported by `klokwrk-project` team members, but are relevant for the project. Those issues are monitored by team members.

When particular issue is resolved, it can be removed from this list when there is no value to monitor it further.

### Github
* [GitHub IS 1017 - Support semi-linear merge option for pull requests](https://github.com/isaacs/github/issues/1017)
  * related to [ADR 0007 - ADR-0007 - Git Workflow with Linear History](../adr/content/0007-git-workflow-with-linear-history.md)
* [GitHub IS 1143 - Rebase and merge pull request option should add a merge commit](https://github.com/isaacs/github/issues/1143)
  * related to [ADR 0007 - ADR-0007 - Git Workflow with Linear History](../adr/content/0007-git-workflow-with-linear-history.md)

### JaCoCo
* [JaCoCo PR 321 - Exceptions cause missed branches in previous lines](https://github.com/jacoco/jacoco/pull/321)
