# klokwrk-tool-gradle-source-repack

`klokwrk-tool-gradle-source-repack` repackages source files from Gradle `all` distribution into an archive understandable for IntelliJ IDEA.

## Introduction
As envisioned by authors, debugging Gradle scripts and internals should be very comfortable from IDEA. One should just place a breakpoint in a script and run a Gradle task in debugging mode.

While this is mainly true for scripts (applied scripts do not work last time I checked), it is quite hard to set up IDEA for debugging Gradle APIs and internal implementations.

The main problem is IDEA's inability to read source archives containing multiple source roots instead of just a single source root. To overcome this, one should repack sources from Gradle
distribution under a single source root and offer such an archive to the IDEA when asked. That repacking part is precisely what `klokwrk-tool-gradle-source-repack` does.

When supplied with correct parameters, `klokwrk-tool-gradle-source-repack` will:
- download Gradle `all` distribution corresponding to the supplied Gradle version
- check SHA-256 of downloaded Gradle distribution file
- repack Gradle source files under a single root in a new `gradle-api-<Gradle version>-sources.jar` archive
- place the `gradle-api-<Gradle version>-sources.jar` archive into `<user-home>/.gradle/caches/<Gradle-version>/generated-gradle-jars/` directory if it exists, or in the current directory otherwise.

## Usage
There are two variants of the tool to choose from:
- standard executable jar
- standalone native utility (compiled with GraalVM native image builder tool)

### Compiling and running a tool from executable jar
To create and run the executable jar:

```
cd <klokwrk-project-dir>/tool/klokwrk-tool-gradle-source-repack
./gradlew clean assemble
java -jar ./build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar <Gradle-version>
```

### Compiling and running a native tool
For compiling into GraalVM native image, you need to have GraalVM JDK 11 active, including its `native-image` build tool. For installing GraalVM it is simplest to use [SDKMAN!](https://sdkman.io/)
utility and install and activate `20.3.0.r11-grl` version of java, for example:

```
sdk install java 20.3.0.r11-grl
sdk use java 20.3.0.r11-grl
```

With GraalVM set up, you need to download its `native-image` tool:

```gu install native-image```

Now you can create a native image for `klokwrk-tool-gradle-source-repack` (this will take several minutes):

```
cd <klokwrk-project-dir>/tool/klokwrk-tool-gradle-source-repack
./gradlew clean assemble
./gradlew kwrkNativeImage
```

With the native image created, use it as follows:

```./build/native-image/klokwrk-tool-gradle-source-repack 6.7.1```

### Additional options
To see (and potentially use) some additional options, provide ``--help`` parameter to the command line:

```
java -jar ./build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --help
./build/native-image/klokwrk-tool-gradle-source-repack --help
```
