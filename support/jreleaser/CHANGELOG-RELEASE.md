## Changelog
This changelog contains a curated list of significant changes between releases. If you need a full changelog with every commit included, please use GitHub or Git log tools.

## Changelog

## ⭐ Features
- [`22cac7b`](https://github.com/croz-ltd/klokwrk-project/commits/22cac7b) feature: Add configurable retry scheduler for CommandGateway {m}
- [`ffaeaff`](https://github.com/croz-ltd/klokwrk-project/commits/ffaeaff) feature: Add support for correlation of unexpected handler exceptions across JVMs {m}
- [`691632e`](https://github.com/croz-ltd/klokwrk-project/commits/691632e) feat: Add graceful shutdown config for embedded web containers in apps
- [`9236252`](https://github.com/croz-ltd/klokwrk-project/commits/9236252) feat: Improve messages and logging of domain exceptions
- [`00efbc6`](https://github.com/croz-ltd/klokwrk-project/commits/00efbc6) feat: Add a demo for improving code coverage with Groovy Slf4j annotation and different logging levels

## ✨ Enhancements
- [`2c6c6f9`](https://github.com/croz-ltd/klokwrk-project/commits/2c6c6f9) enhance: Update formatting rules for jakarta packages
- [`8701d48`](https://github.com/croz-ltd/klokwrk-project/commits/8701d48) enhance: Make logging of handler exceptions a bit more sensible
- [`cddae22`](https://github.com/croz-ltd/klokwrk-project/commits/cddae22) enhance: Add additional changelog categories in JReleaser config {m}

## 🐞 Bug Fixes
- [`de66264`](https://github.com/croz-ltd/klokwrk-project/commits/de66264) fix: Correctly handle nulls and empty strings in some methods of MessageSourceResolvableHelper
- [`63bd7bf`](https://github.com/croz-ltd/klokwrk-project/commits/63bd7bf) fix: Fixing conversion of global index into a string in AxonMessageHelper
- [`f2130f0`](https://github.com/croz-ltd/klokwrk-project/commits/f2130f0) fix: Remove unsupported flags from Docker Compose scripts
- [`f1157fc`](https://github.com/croz-ltd/klokwrk-project/commits/f1157fc) fix: Fix title matching expressions in JReleaser changelog config

## 📔️ Documentation
- docs: Update releaseProcess.md tech note
  - [`a12a2c9`](https://github.com/croz-ltd/klokwrk-project/commits/a12a2c9) docs: Update releaseProcess.md tech note
  - [`bb3cc7d`](https://github.com/croz-ltd/klokwrk-project/commits/bb3cc7d) docs: Update releaseProcess.md tech note
- docs: Update issues document
  - [`79abb33`](https://github.com/croz-ltd/klokwrk-project/commits/79abb33) docs: Update issues document
  - [`a5d39a3`](https://github.com/croz-ltd/klokwrk-project/commits/a5d39a3) docs: Update issues document
  - [`c8c0e05`](https://github.com/croz-ltd/klokwrk-project/commits/c8c0e05) docs: Update issues document
  - [`74676ec`](https://github.com/croz-ltd/klokwrk-project/commits/74676ec) docs: Update issues document
  - [`980d146`](https://github.com/croz-ltd/klokwrk-project/commits/980d146) docs: Update issues document
- [`71ed514`](https://github.com/croz-ltd/klokwrk-project/commits/71ed514) docs: Add ADR-0015 - Handling Exceptions in Distributed CQRS System {m}
- [`bd09427`](https://github.com/croz-ltd/klokwrk-project/commits/bd09427) docs: Add tech note for creating and using unreleased libraries via jitpack.io

## 🕶️ Style
- [`c7c1ae8`](https://github.com/croz-ltd/klokwrk-project/commits/c7c1ae8) style: Polishing - replacing TODO comment with a NOTE
- [`64a840a`](https://github.com/croz-ltd/klokwrk-project/commits/64a840a) style: Polishing - sort & categorize dependencies
- [`5786f76`](https://github.com/croz-ltd/klokwrk-project/commits/5786f76) style: Fix paragraph tags in Groovydoc

## ✅ Test
- [`e751ad7`](https://github.com/croz-ltd/klokwrk-project/commits/e751ad7) test: Improve tests of repack tool {m}
- [`fc38d6b`](https://github.com/croz-ltd/klokwrk-project/commits/fc38d6b) test: Improve tests for ResponseFormattingConstraintViolationExceptionHandler
- [`ca82937`](https://github.com/croz-ltd/klokwrk-project/commits/ca82937) test: Improve tests for ResponseFormattingDomainExceptionHandler
- [`bad1678`](https://github.com/croz-ltd/klokwrk-project/commits/bad1678) test: Fixing tests for Location map constructor

## ⛏️ Build
- [`ac81f9f`](https://github.com/croz-ltd/klokwrk-project/commits/ac81f9f) build(klokwrk-tool-gradle-source-repack): Resolve "too long command" issue on Windows for GraalVM native image build
- [`78fad6d`](https://github.com/croz-ltd/klokwrk-project/commits/78fad6d) build: Update CodeNarc rules
- build: Update CodeNarc
  - [`4da5c2b`](https://github.com/croz-ltd/klokwrk-project/commits/4da5c2b) build: Update CodeNarc to 4e516637ee version self-published on Jitpack.io
  - [`4e9a8b6`](https://github.com/croz-ltd/klokwrk-project/commits/4e9a8b6) build: Update CodeNarc to e873ff429e version self-published on Jitpack.io
  - [`6bf04d2`](https://github.com/croz-ltd/klokwrk-project/commits/6bf04d2) build: Update CodeNarc to 0d01347e22 version self-published on Jitpack.io
  - [`a9b9c59`](https://github.com/croz-ltd/klokwrk-project/commits/a9b9c59) build: Update CodeNarc to 76f51b3b99 version self-published on Jitpack.io
  - [`acc6e5f`](https://github.com/croz-ltd/klokwrk-project/commits/acc6e5f) build: Update CodeNarc to 5a2472719c version self-published on Jitpack.io

## 🏭️ CI
- [`9b34437`](https://github.com/croz-ltd/klokwrk-project/commits/9b34437) ci(github-action): Switch to windows-latest for building native image
- [`ef7b309`](https://github.com/croz-ltd/klokwrk-project/commits/ef7b309) ci(github-action): Rename eskatos/gradle-command-action into gradle/gradle-build-action
- [`fc24ade`](https://github.com/croz-ltd/klokwrk-project/commits/fc24ade) ci(github-actions): Upgrade Codecov GitHub Action to v2 major version
- [`72b0191`](https://github.com/croz-ltd/klokwrk-project/commits/72b0191) ci(github-actions): Fix GraalVM native image build for Windows
- [`0d711c5`](https://github.com/croz-ltd/klokwrk-project/commits/0d711c5) ci(github-actions): Remove deprecations from JReleaser config

## 😰 Refactor
- [`2b91b5a`](https://github.com/croz-ltd/klokwrk-project/commits/2b91b5a) refactor: Rename property ViolationCode.codeAsText into codeKey
- [`05cb709`](https://github.com/croz-ltd/klokwrk-project/commits/05cb709) refactor: Refactor handling of HandlerExecutionExceptions {m}
- [`292f38e`](https://github.com/croz-ltd/klokwrk-project/commits/292f38e) refactor: Replace deprecated api in logging handler enhancer definitions

## ♾️ Dependencies
- deps: Bump Axon Server
  - [`8b6d49a`](https://github.com/croz-ltd/klokwrk-project/commits/8b6d49a) deps: Bump Axon Server to 4.5.8 version
  - [`2bd144d`](https://github.com/croz-ltd/klokwrk-project/commits/2bd144d) deps: Bump Axon Server to 4.5.7 version
  - [`8d2e9c6`](https://github.com/croz-ltd/klokwrk-project/commits/8d2e9c6) deps: Bump Axon Server to 4.5.6 version
  - [`c319046`](https://github.com/croz-ltd/klokwrk-project/commits/c319046) deps: Bump Axon Server to 4.5.3 version
- deps: Upgrade Graal VM
  - [`75c597c`](https://github.com/croz-ltd/klokwrk-project/commits/75c597c) deps: Upgrade Graal VM to 21.3 version
  - [`f6ab9eb`](https://github.com/croz-ltd/klokwrk-project/commits/f6ab9eb) deps: Upgrade Graal VM to 21.2 version
- [`f0a4720`](https://github.com/croz-ltd/klokwrk-project/commits/f0a4720) deps: Upgrade Gradle Shadow plugin to 7.1.0 version
- [`74c97e6`](https://github.com/croz-ltd/klokwrk-project/commits/74c97e6) deps: Upgrade Kordamp Gradle Plugins to 0.47.0 version
- deps: Bump Testcontainers
  - [`a42f554`](https://github.com/croz-ltd/klokwrk-project/commits/a42f554) deps: Bump Testcontainers to 1.16.2 version
  - [`d0f3fb5`](https://github.com/croz-ltd/klokwrk-project/commits/d0f3fb5) deps: Bump Testcontainers to 1.16.1 version
  - [`9a318b6`](https://github.com/croz-ltd/klokwrk-project/commits/9a318b6) deps: Bump Testcontainers to 1.16.0 version
- deps: Bump Spring Boot
  - [`8a667ed`](https://github.com/croz-ltd/klokwrk-project/commits/8a667ed) deps: Bump Spring Boot to 2.5.6 version
  - [`a12258e`](https://github.com/croz-ltd/klokwrk-project/commits/a12258e) deps: Bump Spring Boot to 2.5.5 version
  - [`41d94d0`](https://github.com/croz-ltd/klokwrk-project/commits/41d94d0) deps: Bump Spring Boot to 2.5.3 version
  - [`0221fa5`](https://github.com/croz-ltd/klokwrk-project/commits/0221fa5) deps: Bump Spring Boot to 2.5.2 version
- deps: Bump Micronaut Gradle Plugin
  - [`c1db66c`](https://github.com/croz-ltd/klokwrk-project/commits/c1db66c) deps: Bump Micronaut Gradle Plugin to 2.0.7 version
  - [`fa896da`](https://github.com/croz-ltd/klokwrk-project/commits/fa896da) deps: Bump Micronaut Gradle Plugin to 2.0.3 version
  - [`d475b90`](https://github.com/croz-ltd/klokwrk-project/commits/d475b90) deps: Bump Micronaut Gradle Plugin to 2.0.2 version
- deps: Upgrade Micronaut
  - [`6c6279e`](https://github.com/croz-ltd/klokwrk-project/commits/6c6279e) deps: Upgrade Micronaut to 3.1.1 version {m}
  - [`a0cf1cd`](https://github.com/croz-ltd/klokwrk-project/commits/a0cf1cd) deps: Bump Micronaut to 2.5.11 version
  - [`831bfed`](https://github.com/croz-ltd/klokwrk-project/commits/831bfed) deps: Bump Micronaut to 2.5.7 version
- [`d4fe053`](https://github.com/croz-ltd/klokwrk-project/commits/d4fe053) deps: Upgrade Codenarc to 2.2.0 version
- deps: Bump PostgreSQL
  - [`c921804`](https://github.com/croz-ltd/klokwrk-project/commits/c921804) deps: Bump PostgreSQL to 14.0 version
  - [`a86bbdf`](https://github.com/croz-ltd/klokwrk-project/commits/a86bbdf) deps: Bump PostgreSQL to 13.3 version
- deps: Bump Axon Framework
  - [`79caacf`](https://github.com/croz-ltd/klokwrk-project/commits/79caacf) deps: Bump Axon Framework to 4.5.4 version
  - [`1c661ed`](https://github.com/croz-ltd/klokwrk-project/commits/1c661ed) deps: Bump Axon Framework to 4.5.3 version
- deps: Bump ArchUnit
  - [`517b6d2`](https://github.com/croz-ltd/klokwrk-project/commits/517b6d2) deps: Bump ArchUnit to 0.21.0 version
  - [`74d180b`](https://github.com/croz-ltd/klokwrk-project/commits/74d180b) deps: Bump ArchUnit to 0.20.1 version
- deps: Bump ClassGraph
  - [`fb71935`](https://github.com/croz-ltd/klokwrk-project/commits/fb71935) deps: Bump ClassGraph to 4.8.128 version
  - [`0ee98ae`](https://github.com/croz-ltd/klokwrk-project/commits/0ee98ae) deps: Bump ClassGraph to 4.8.113 version
  - [`1f16992`](https://github.com/croz-ltd/klokwrk-project/commits/1f16992) deps: Bump ClassGraph to 4.8.109 version
- [`f93e108`](https://github.com/croz-ltd/klokwrk-project/commits/f93e108) deps: Upgrade Groovy to 3.0.9 version
- [`c9f1121`](https://github.com/croz-ltd/klokwrk-project/commits/c9f1121) deps: Bump Test Retry Gradle plugin to 1.3.1 version

## ◻️️ Miscellaneous
- [`91ec141`](https://github.com/croz-ltd/klokwrk-project/commits/91ec141) misc: Add missing licenses
- [`f5d2c9b`](https://github.com/croz-ltd/klokwrk-project/commits/f5d2c9b) misc(CodeNarc): Apply enhanced BracesForMethod rule which allows braces on next line for multiline declarations
- [`33b6110`](https://github.com/croz-ltd/klokwrk-project/commits/33b6110) misc(CodeNarc): Add CodeNarc prefix to all CodeNarc SuppressWarnings annotations
- [`d36d7e7`](https://github.com/croz-ltd/klokwrk-project/commits/d36d7e7) misc(CodeNarc): Apply fixed SpaceBeforeClosingBrace CodeNarc rule
- [`cd102dc`](https://github.com/croz-ltd/klokwrk-project/commits/cd102dc) misc(CodeNarc): Apply fixed SpaceAfterClosingBrace CodeNarc rule
- [`66f786a`](https://github.com/croz-ltd/klokwrk-project/commits/66f786a) misc(CodeNarc): Apply fixed MissingBlankLineBeforeAnnotatedField CodeNarc rule
- [`7d461ed`](https://github.com/croz-ltd/klokwrk-project/commits/7d461ed) misc(CodeNarc): Apply fixed SpaceInsideParentheses CodeNarc rule

## Contributors
We'd like to thank the following people for their contributions:
- Damir Murat ([@dmurat](https://github.com/dmurat))
