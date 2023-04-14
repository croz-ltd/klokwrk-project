# Making a release
- do all necessary testing of draft release
- make sure the latest JReleaser version is installed locally. If not, install it (or upgrade it) via SDKMAN

      sdk install jreleaser

### **feature branch** (i.e., `feature_preparingRelease`)
- manually generate changelog. From project root with the latest **JReleaser release**

      env JRELEASER_PROJECT_VERSION=1.5.1 JRELEASER_GITHUB_TOKEN=1 \
      jreleaser changelog --basedir=. --config-file=./support/jreleaser/jreleaser-draft.yml --debug

  If you need to use the latest **JReleaser snapshot release**, it can be started via jbang installed locally (also via SDKMAN)

      env JRELEASER_PROJECT_VERSION=1.5.2-SNAPSHOT JRELEASER_GITHUB_TOKEN=1 \
      jbang --verbose jreleaser-snapshot@jreleaser changelog --basedir=. --config-file=./support/jreleaser/jreleaser-draft.yml --debug

  When using JReleaser via jbang, and you need the latest release or snapshot version, before executing command make sure the cached JReleaser version is deleted from maven cache
  and from jbang cache

      rm -rf ~/.m2/repository/com/github/jreleaser
      rm -rf ~/.m2/repository/org/jreleaser
      jbang cache clear

- Verify generated changelog and make all necessary updates like:
  - delete uncategorized commits
  - deduplicate `deps` commits so that only the latest upgrade of particular dependency is included
  - add few sentences of release description if needed
  - fix any invalid commit message metadata (i.e. {m} from non-merge commits)
- put all content of prepared changelog in `support/jreleaser/CHANGELOG-RELEASE.md` file. That file is use for creating a changelog during real release creation on GitHub.
- commit `support/jreleaser/CHANGELOG-RELEASE.md` in `feature_preparingRelease`
- change versions of of klokwrk application images.
- commit properties file with updated versions in `feature_preparingRelease`

  Do note that build will not work after this commit. It will work again once `feature_preparingRelease` branch is merged into main branch and tagged with release tag.

### **master branch**
- merge (`--no-ff`) `feature_preparingRelease` into local `master`. Use the message in the following format:

      notype: Prepare release 0.0.5 {m}

- local - make an empty commit that will carry annotated release tag. Use the message in following format: `notype(project): Release 0.0.5`

      git commit --allow-empty -m "notype: Release 0.0.5"

- local - tag previous commit with the version tag

      git tag -a v0.0.5 -m "Release 0.0.5"

- local full build (oracle JVM)

      gw clean assemble testClasses testIntegrationClasses testComponentClasses --parallel -x groovydoc
      gw bootBuildImage
      gw test --parallel
      gw testIntegration --parallel
      gw testComponent --parallel
      gw aggregateCodenarc
      gw aggregateJacocoReport
      gw aggregateGroovydoc
      gw allTestUnitReports
      gw allTestIntegrationReports
      gw allTestComponentReports

- local test run of applications
- local build of GraalVM native image (GraalVM)

       gw kwrkNativeImage

    - test local native image manually

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --help --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

- upload docker images
- commit local changes on remote with annotated tags

      git push --follow-tags

- let Continuous integration workflow finish
- run GitHub workflow: klokwrk-tool-gradle-source-repack GraalVM native image builder
- run GitHub workflow: Release workflow with:
    - version (without 'v' prefix): `0.0.5`
    - release type: `release`

# Preparing for next development cycle after release
### **feature branch** (i.e., `feature_preparingNextDevCycle`)
- update and commit new versions of klokwrk application images
- remove content from and commit `support/jreleaser/CHANGELOG-RELEASE.md` file
- local full build (oracle JVM)

      gw clean assemble testClasses testIntegrationClasses testComponentClasses --parallel -x groovydoc
      gw bootBuildImage
      gw test --parallel
      gw testIntegration --parallel
      gw testComponent --parallel
      gw aggregateCodenarc
      gw aggregateJacocoReport
      gw aggregateGroovydoc
      gw allTestUnitReports
      gw allTestIntegrationReports
      gw allTestComponentReports

- local test run of applications
- local build of GraalVM native image (GraalVm)

      gw kwrkNativeImage

    - test local native image manually

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --help --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

          ./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
          --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.3

- upload docker images
- optionally verify that changes in the branch work correctly
  - push `feature_preparingNextDevCycle` branch
  - let Continuous integration workflow finish
  - run GitHub workflow: klokwrk-tool-gradle-source-repack GraalVM native image builder
  - run GitHub workflow: Release workflow with
    - version (without 'v' prefix): `0.0.6-SNAPSHOT`
    - release type: `draft`

### **master branch**
- merge (`--no-ff`) `feature_preparingNextDevCycle` into master. Use the message in the following format:

      notype: Prepare next development cycle {m}

- push master
- delete local and remote `feature_preparingNextDevCycle` branch
- let Continuous integration workflow finish
- run GitHub workflow: klokwrk-tool-gradle-source-repack GraalVM native image builder (can be skipped if time is an issue)
- run GitHub workflow: Release workflow with (can be skipped if time is an issue):
  - version (without 'v' prefix): `0.0.6-SNAPSHOT`
  - release type: `draft`
