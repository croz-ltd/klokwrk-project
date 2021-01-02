# klokwrk-tool-gradle-source-repack

`klokwrk-tool-gradle-source-repack` repackages Gradle source files from Gradle `all` distribution into a JAR archive that can be used as sources repository from IntelliJ IDEA when debugging Gradle
internals.

To see how `klokwrk-tool-gradle-source-repack` fits in Gradle debugging from IDEA, take a look at the
"[Debugging Gradle internals from IntelliJ IDEA](../../support/documentation/article/debugging-gradle-from-idea/debugging-gradle-from-idea.md)" article.

## Introduction
As envisioned by authors, debugging Gradle scripts, 3rd party plugins, and internal Gradle classes should be easy from IDEA. One should just place a breakpoint in a script or class and run a Gradle
task in debugging mode. While this is mainly true for Gradle scripts, debugging Gradle internal classes does not work out-of-the-box.

The main problem is IDEA's inability to read source archives containing multiple source roots instead of just a single source root. To overcome this, one should repack sources from Gradle
distribution under a single source root and offer such an archive to the IDEA when asked during the debugging process. That repackaging part is what `klokwrk-tool-gradle-source-repack` can help with.

When supplied with correct parameters, `klokwrk-tool-gradle-source-repack` will:
- download Gradle `all` distribution corresponding to the supplied Gradle version
- check SHA-256 of downloaded Gradle distribution file
- repack Gradle source files under a single root in a new `gradle-api-<Gradle version>-sources.jar` archive
- place the `gradle-api-<Gradle version>-sources.jar` archive into `<user-home>/.gradle/caches/<Gradle-version>/generated-gradle-jars/` directory if that directory exists, or in the current directory
  otherwise.

## Download
There are two variants of the `klokwrk-tool-gradle-source-repack` to choose from: an executable JAR and standalone native utility (compiled with GraalVM native image builder tool).
You can download the latest versions from the "Artifacts" section of the newest successful build. To do that, login into your GitHub account (otherwise, download links will not be available) and
then:
- Go to the home page of [klokwrk-project](https://github.com/croz-ltd/klokwrk-project).
- Click on the "Actions" menu.
- Navigate further on:
  - "klokwrk-tool-gradle-source-repack GraalVM native image builder" workflow.
  - Choose the latest successful build of the workflow.
  - Select the desired distribution from the "Artifacts" section.

After the download, place the tool in any desired directory. For running the JAR variant, you'll need Java 11 SDK. For the native tool, there are no additional requirements.

## Usage
### Standard options
To see standard options and parameters, execute the tool with the `--help` option:

```
./klokwrk-tool-gradle-source-repack --help

Usage: klokwrk-tool-gradle-source-repack [-hV] [-c=<true|false>]
       [-l=loggerName=loggingLevel[,loggerName=loggingLevel...]]...
       <gradle-version>
Downloads Gradle source distribution and repackages it in a JAR suitable to use
for debugging Gradle internals in IDEA.
      <gradle-version>   Gradle version to use.
  -c, --cleanup=<true|false>
                         Removing downloaded files after successful execution.
                           Default: true
  -h, --help             Show this help message and exit.
  -l, --loggingLevels=loggerName=loggingLevel[,loggerName=loggingLevel...]
                         Optional comma separated list of logger levels
                           configurations. Changing log levels is effective
                           only after a command starts running. All preceding
                           logging is not affected. Example: ROOT=INFO,org.
                           klokwrk.tool.gradle.source.repack=DEBUG,io.micronaut.
                           http.client=DEBUG,etc...
  -V, --version          Print version information and exit.
```

- `gradle-version` parameter is required. It supplies an officially released version of Gradle. A full list of Gradle distributions can be found at https://services.gradle.org/distributions/ .
- `--cleanup` option controls if downloaded files are removed (`true`) or not (`false`). Default is `true`.
- `--loggingLevels` option enables specifying logging levels for Logback loggers. It should be noted that changing default logging levels can be done only when the tool starts execution. This means
  that logging levels for loggers used in bootstrapping code (i.e., Micronaut DI container initialization) cannot be changed. The main reason for this is that Logback needs to be initialized at build
  time during the creation of GraalVM native image, which prevents Logback's native means of dynamic configuration.

### Hidden options
Several hidden options are not displayed in the help message. These options should not be used in normal circumstances but are helpful for testing and debugging. None of these options does have a
short variant.

- `--gradle-distribution-dir-url` - URL to the directory where Gradle distribution resides. Default is `"https://services.gradle.org/distributions/"`.
- `--download-dir` - Directory where downloaded files are placed. Default is a current directory (a directory from where the tool was started).
- `--repackDir` - Directory where the archive with repacked sources will be placed. It defaults to `~/.gradle/caches/${ this.gradleVersion }/generated-gradle-jars` if that directory exists, or to
  the current working directory otherwise.

### Examples
- Downloading and repacking Gradle version `6.7.1`:
  ```
  ./klokwrk-tool-gradle-source-repack 6.7.1

  Downloading 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip': 100%
  Downloading 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip.sha256': 100%
  SHA-256 checksum OK.
  Repackaging into /Users/username/.gradle/caches/6.7.1/generated-gradle-jars/gradle-api-6.7.1-sources.jar: 100%
  ```

- Using custom configuration for loggers and turning off removal of downloaded files:
  ```
  ./klokwrk-tool-gradle-source-repack --loggingLevels=org.klokwrk.tool.gradle.source.repack=INFO --cleanup=false 6.7.1

  13:03:35.037 [main] INFO  o.k.t.g.s.r.d.GradleDownloader - Downloading: 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip' ==> '/Users/username/testing/gradle-6.7.1-all.zip'.
  Downloading 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip': 100%
  13:03:44.909 [main] INFO  o.k.t.g.s.r.d.GradleDownloader - Downloading: 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip.sha256' ==> '/Users/username/testing/gradle-6.7.1-all.zip.sha256'.
  Downloading 'https://downloads.gradle-dn.com/distributions/gradle-6.7.1-all.zip.sha256': 100%
  SHA-256 checksum OK.
  13:03:46.181 [main] INFO  o.k.t.g.s.r.r.GradleSourceRepackager - Repackaging Gradle sources: /Users/username/testing/gradle-6.7.1-all.zip ===> /Users/username/.gradle/caches/6.7.1/generated-gradle-jars/gradle-api-6.7.1-sources.jar
  Repackaging into /Users/username/.gradle/caches/6.7.1/generated-gradle-jars/gradle-api-6.7.1-sources.jar: 100%
  ```

## Building the tool
### Compiling and running a tool from the executable jar
To create and run the executable jar:

```
cd <klokwrk-project-dir>/tool/klokwrk-tool-gradle-source-repack
../../gradlew clean assemble
java -jar ./build/libs/klokwrk-tool-gradle-source-repack-<tool-version>-all.jar <Gradle-version>
```

### Compiling and running a native tool
For compiling into GraalVM native image, you need to have GraalVM JDK 11 active, including its `native-image` build tool. For building the native image on your OS, GraalVM native-image requirements
need to be satisfied - https://www.graalvm.org/reference-manual/native-image/#prerequisites .

For installing GraalVM, it is simplest to use [SDKMAN!](https://sdkman.io/) utility from which one should install and activate `20.3.0.r11-grl` version of java, for example:

```
sdk install java 20.3.0.r11-grl
sdk use java 20.3.0.r11-grl
```

With GraalVM set up, for creating the native image, downloading of GraalVM `native-image` tool is required:

```gu install native-image```

Now, the native image for `klokwrk-tool-gradle-source-repack` can be created with the following commands (this will take several minutes):

```
cd <klokwrk-project-dir>/tool/klokwrk-tool-gradle-source-repack
../../gradlew clean assemble
../../gradlew kwrkNativeImage
```

When the native image build finishes, the tool can be used as follows:

```./build/native-image/klokwrk-tool-gradle-source-repack 6.7.1```
