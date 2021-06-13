## Changelog
This changelog contains a curated list of significant changes between releases. If you need a full changelog with every commit included, please use GitHub or Git log tools.

## ⭐ Features
- [`5b32428`](https://github.com/croz-ltd/klokwrk-project/commits/5b32428) feat: Add TestRetryDisabler Gradle plugin {m}
- [`a1a72b8`](https://github.com/croz-ltd/klokwrk-project/commits/a1a72b8) feat: Add changelog generator via JReleaser
- [`5c9e606`](https://github.com/croz-ltd/klokwrk-project/commits/5c9e606) feat: Add git hooks for verifying the title of commit messages

## 📔️ Documentation
- [`a89e36b`](https://github.com/croz-ltd/klokwrk-project/commits/a89e36b) docs: Rename labels in issue templates
- [`fd0826f`](https://github.com/croz-ltd/klokwrk-project/commits/fd0826f) docs: Fix location and name of pull request template
- [`010cf78`](https://github.com/croz-ltd/klokwrk-project/commits/010cf78) docs: Add open source community health files {m}
- [`3817860`](https://github.com/croz-ltd/klokwrk-project/commits/3817860) docs: Fix typos in ADR file names {m}
- [`898477c`](https://github.com/croz-ltd/klokwrk-project/commits/898477c) docs: Add documentation for TestRetryDisablerPlugin Gradle plugin
- [`dab5a92`](https://github.com/croz-ltd/klokwrk-project/commits/dab5a92) docs: Add basic instructions for contributing
- [`4d905cc`](https://github.com/croz-ltd/klokwrk-project/commits/4d905cc) docs: Add "ADR-0014 - Commit message format"
- [`b756832`](https://github.com/croz-ltd/klokwrk-project/commits/b756832) docs: Update technical note releaseProcess.md
- [`048ca56`](https://github.com/croz-ltd/klokwrk-project/commits/048ca56) docs: Add release related updates {m}
- [`d28b49e`](https://github.com/croz-ltd/klokwrk-project/commits/d28b49e) docs: Add tech-note about release process

## ✅ Test
- [`5094498`](https://github.com/croz-ltd/klokwrk-project/commits/5094498) test: Rename invalid SHA file
- [`6467e06`](https://github.com/croz-ltd/klokwrk-project/commits/6467e06) test(klokwrk-lib-archunit): Update tests for slight coverage improvement

## ⛏️ Build
- [`45a0643`](https://github.com/croz-ltd/klokwrk-project/commits/45a0643) build: Refactor Gradle configuration to be more idiomatic and reusable {m}
- [`feaf55a`](https://github.com/croz-ltd/klokwrk-project/commits/feaf55a) build: Extract component tests into separate source set {m}
- [`f94cf17`](https://github.com/croz-ltd/klokwrk-project/commits/f94cf17) build: Add custom test report tasks for cumulative reporting based on test types {m}
- [`cdf3547`](https://github.com/croz-ltd/klokwrk-project/commits/cdf3547) build: Update licensing plugin configuration
- [`9bfca70`](https://github.com/croz-ltd/klokwrk-project/commits/9bfca70) build: Remove gradle-taskinfo plugin
- [`efc4a56`](https://github.com/croz-ltd/klokwrk-project/commits/efc4a56) build: Extract configuration of Groovy modules into precompiled Gradle script {m}
- [`aad61e5`](https://github.com/croz-ltd/klokwrk-project/commits/aad61e5) build: Extract base configuration of modules precompiled Gradle script {m}
- [`88d27e5`](https://github.com/croz-ltd/klokwrk-project/commits/88d27e5) build: Extract configuration of Spring Boot apps into precompiled Gradle script {m}
- [`5bae582`](https://github.com/croz-ltd/klokwrk-project/commits/5bae582) build: Extract configuration of integration tests into precompiled Gradle script {m}

## 🏭️ CI
- [`c637059`](https://github.com/croz-ltd/klokwrk-project/commits/c637059) ci(github-actions): Add component tests with reporting
- [`00da66c`](https://github.com/croz-ltd/klokwrk-project/commits/00da66c) ci(github-actions): Add integration test reporting
- [`dec216b`](https://github.com/croz-ltd/klokwrk-project/commits/dec216b) ci(github-actions): Update release workflow to use latest released version of JReleaser
- [`ea34487`](https://github.com/croz-ltd/klokwrk-project/commits/ea34487) ci(github-actions): Update JamesIves/github-pages-deploy-action to v4 major release
- [`86bb64f`](https://github.com/croz-ltd/klokwrk-project/commits/86bb64f) ci(github-actions): Update actions/setup-java to v2 major release
- [`c6e2624`](https://github.com/croz-ltd/klokwrk-project/commits/c6e2624) ci(github-actions): Refactor, repair, and speed up the GraalVM native image build for Windows

## 🧰 Tasks
- [`b4771a8`](https://github.com/croz-ltd/klokwrk-project/commits/b4771a8) task: Update issues document
- [`eea9b50`](https://github.com/croz-ltd/klokwrk-project/commits/eea9b50) task: Update Structure 101 and Sonargraph projects
- [`84d00aa`](https://github.com/croz-ltd/klokwrk-project/commits/84d00aa) task: Add missing licenses

## ♾️ Dependencies
- [`c2e4814`](https://github.com/croz-ltd/klokwrk-project/commits/c2e4814) deps: Bump Spring Boot to 2.5.1 version
- [`7833450`](https://github.com/croz-ltd/klokwrk-project/commits/7833450) deps: Bump Wavefront for Spring Boot to 2.2.0 version
- [`79380ca`](https://github.com/croz-ltd/klokwrk-project/commits/79380ca) deps: Bump Axon Server to 4.5.2 version
- [`49377f4`](https://github.com/croz-ltd/klokwrk-project/commits/49377f4) deps: Bump Micronaut Gradle Plugin to 1.5.1 version
- [`3063d64`](https://github.com/croz-ltd/klokwrk-project/commits/3063d64) deps: Bump Micronaut to 2.5.5 version
- [`d1019a2`](https://github.com/croz-ltd/klokwrk-project/commits/d1019a2) deps: Bump ClassGraph to 4.6.108 version
- [`dcd79ca`](https://github.com/croz-ltd/klokwrk-project/commits/dcd79ca) deps: Upgrade to Gradle 7.0.2 {m}
- [`6c33c0e`](https://github.com/croz-ltd/klokwrk-project/commits/6c33c0e) deps: Override License Gradle Plugin version to 0.16.1 for Gradle 7 compatibility
- [`030656b`](https://github.com/croz-ltd/klokwrk-project/commits/030656b) deps: Bump Gradle Shadow plugin to 7.0.0 version
- [`77ba36b`](https://github.com/croz-ltd/klokwrk-project/commits/77ba36b) deps: Upgrade Kordamp Gradle Plugins to 0.46.0 version {m}
- [`b97eb95`](https://github.com/croz-ltd/klokwrk-project/commits/b97eb95) deps: Bump ArchUnit to 0.19.0 version
- [`c67a966`](https://github.com/croz-ltd/klokwrk-project/commits/c67a966) deps: Bump Axon Framework to 4.5.1 version
- [`59ce925`](https://github.com/croz-ltd/klokwrk-project/commits/59ce925) deps: Upgrade Spring Boot to 2.5.0 version {m}
- [`6c85d18`](https://github.com/croz-ltd/klokwrk-project/commits/6c85d18) deps: Bump WireMock to 2.28.0 version
- [`6f7e4df`](https://github.com/croz-ltd/klokwrk-project/commits/6f7e4df) deps: Bump JaCoCo to 0.8.7 version
- [`8e46bd7`](https://github.com/croz-ltd/klokwrk-project/commits/8e46bd7) deps: Bump Spock Framework to 2.0 version
- [`e54f03c`](https://github.com/croz-ltd/klokwrk-project/commits/e54f03c) deps: Bump Test Retry Gradle plugin to 1.2.1 version

## 🧐️ Articles
- [`21afc52`](https://github.com/croz-ltd/klokwrk-project/commits/21afc52) article(startingUp): Update with up-to-date commands and procedures

## Contributors
Damir Murat
