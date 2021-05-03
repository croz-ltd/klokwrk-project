# Making a release
- do all necessary testing of draft release

### **feature branch** (i.e., `feature_preparingRelease`)
- update JReleaser with `draft: false`
- update versions of klokwrk application images

### **master branch**
- merge (`--no-ff`) `feature_preparingRelease` into local `master`
- local - empty commit, i.e., `Release 0.0.4`

      git commit --allow-empty -m "Release 0.0.4"

- local - tag previous commit with the version tag

      git tag -a v0.0.4 -m "Tagging Release 0.0.4"

- local full build (oracle JVM)

      gw clean assemble testClasses testIntegrationClasses --parallel -x groovydoc
      gw bootBuildImage
      gw testIntegration --parallel

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
- run GitHub workflow: Release workflow with new release version (without 'v' prefix)

# Preparing for next development cycle after release
### **feature branch** (i.e., `feature_preparingNextDevCycle`)
- update and commit JReleaser with `draft: true`
- update and commit new versions of klokwrk application images
- local full build (oracle JVM)

      gw clean assemble testClasses testIntegrationClasses --parallel -x groovydoc
      gw bootBuildImage
      gw testIntegration --parallel

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
- push `feature_preparingNextDevCycle` branch
- let Continuous integration workflow finish
- run GitHub workflow: klokwrk-tool-gradle-source-repack GraalVM native image builder
- run GitHub workflow: Release (draft) workflow with new SNAPSHOT version (without 'v' prefix)

### **master branch**
- merge (`--no-ff`) `feature_preparingNextDevCycle` into master
- push master
- delete local and remote `feature_preparingNextDevCycle` branch
- let Continuous integration workflow finish
- run GitHub workflow: klokwrk-tool-gradle-source-repack GraalVM native image builder (can be skipped if time is an issue)
- run GitHub workflow: Release (draft) workflow with new SNAPSHOT version (without 'v' prefix) (can be skipped if time is an issue)
