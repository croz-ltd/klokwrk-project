/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.tool.gradle.source.repack

import groovy.transform.CompileStatic
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.logging.LogLevel
import io.micronaut.logging.LoggingSystem
import org.klokwrk.tool.gradle.source.repack.checksum.GradleSha256CheckInfo
import org.klokwrk.tool.gradle.source.repack.checksum.GradleSha256Checker
import org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider
import org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader
import org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloaderInfo
import org.klokwrk.tool.gradle.source.repack.repackager.GradleSourceRepackager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility
import picocli.CommandLine.Model
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import jakarta.inject.Inject
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.util.regex.Pattern

@SuppressWarnings("CodeNarc.JavaIoPackageAccess")
@Command(
    name = "klokwrk-tool-gradle-source-repack",
    description = "Downloads Gradle source distribution and repackages it in a JAR suitable to use for debugging Gradle internals in IDEA.",
    mixinStandardHelpOptions = true,
    versionProvider = PropertiesVersionProvider
)
@CompileStatic
class GradleSourceRepackCommand implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(GradleSourceRepackCommand)

  private static final String GRADLE_VERSION_REGEX_FORMAT = /([2-9]\d*){1}(\.\d+){1}(\.\d+)?(-(rc|milestone)-[1-9]\d*)?/
  private static final Pattern GRADLE_VERSION_REGEX_PATTERN = ~GRADLE_VERSION_REGEX_FORMAT

  private static final String OPTION_LOGGING_LEVEL_CONFIG_LIST_DESCRIPTION = """\
Optional comma separated list of logger levels configurations. Changing log levels is effective only after a command starts running. All preceding logging is not affected. \
Example: ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG,io.micronaut.http.client=DEBUG,etc..."""

  static void main(String[] args) throws Exception {
    PicocliRunner.run(GradleSourceRepackCommand, args)
  }

  private String cliParameterGradleVersion

  @Spec
  private Model.CommandSpec commandSpec

  @SuppressWarnings("unused")
  @Parameters(paramLabel = "<gradle-version>", description = "Gradle version to use.")
  void setCliParameterGradleVersion(String gradleVersion) {
    if (!(gradleVersion ==~ GRADLE_VERSION_REGEX_PATTERN)) {
      throw new ParameterException(
          commandSpec.commandLine(), "Invalid value '${ gradleVersion }' for parameter '<gradle-version>'. Value should comply with regex '${ GRADLE_VERSION_REGEX_FORMAT }'."
      )
    }

    this.cliParameterGradleVersion = gradleVersion
  }

  @Option(names = ["-c", "--cleanup"], description = "Removing downloaded files after successful execution.", showDefaultValue = Visibility.ALWAYS, arity = "1", paramLabel = "<true|false>")
  Boolean cliOptionCleanup = true

  @Option(
      names = ["-l", "--loggingLevels"], showDefaultValue = Visibility.ON_DEMAND, split = ",",
      description = OPTION_LOGGING_LEVEL_CONFIG_LIST_DESCRIPTION,
      paramLabel = "loggerName=loggingLevel"
  )
  List<String> loggingLevelConfigList

  @Option(names = ["--gradle-distribution-dir-url"], description = "URL to the directory where Gradle distribution resides.", hidden = true)
  String gradleDistributionDirUrl

  @Option(names = ["--download-dir"], description = "Path to the directory where Gradle distribution with sources will be downloaded.", hidden = true)
  String downloadDir

  @Option(names = ["--repack-dir"], description = "Path to the directory where Gradle distribution with sources will be repacked.", hidden = true)
  String repackDir

  @Inject
  GradleDownloader gradleDownloader

  @Inject
  LoggingSystem loggingSystem

  @SuppressWarnings('GroovyPointlessBoolean')
  @Override
  void run() {
    configureCustomLoggingLevels()
    log.debug("Started.")

    GradleSourceRepackCliArguments cliArguments = makeGradleSourceRepackCliArguments(FileSystems.default)
    log.debug("cliArguments: {}", cliArguments)

    File gradleDistributionZipFile = fetchGradleDistributionZipFile(cliArguments, gradleDownloader)
    File gradleDistributionZipSha256File = fetchGradleDistributionZipSha256File(cliArguments, gradleDownloader)

    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(gradleDistributionZipSha256File, gradleDistributionZipFile)
    if (gradleSha256CheckInfo.isMatch() == true) {
      printlnOnConsole "SHA-256 checksum OK."
    }
    else {
      String message = "SHA-256 does not match [fetched: ${ gradleSha256CheckInfo.fetchedSha256 }, calculated: ${ gradleSha256CheckInfo.calculatedSha256 }]. Cannot continue."
      throw new IllegalStateException(message)
    }

    GradleSourceRepackager.repackGradleSource(cliArguments.toGradleSourceRepackagerInfo(gradleDistributionZipFile.absolutePath))

    if (cliArguments.performCleanup == true) {
      cleanDownloadedFiles([gradleDistributionZipFile, gradleDistributionZipSha256File])
    }

    log.debug("Finished.")
  }

  void configureCustomLoggingLevels() {
    if (loggingLevelConfigList) {
      loggingLevelConfigList.each { String loggingLevelConfig ->
        List<String> loggingLevelConfigTokenList = loggingLevelConfig.tokenize("=")
        if (loggingLevelConfigTokenList.size() != 2) {
          throw new IllegalArgumentException("--loggingLevels option contains invalid configuration: '$loggingLevelConfig'. Cannot continue.")
        }

        String logger = loggingLevelConfigTokenList[0]
        String level = loggingLevelConfigTokenList[1].toUpperCase()
        loggingSystem.setLogLevel(logger, LogLevel.valueOf(level))
      }
    }
  }

  GradleSourceRepackCliArguments makeGradleSourceRepackCliArguments(FileSystem fileSystem) {
    GradleSourceRepackCliArguments cliArguments = new GradleSourceRepackCliArguments(cliParameterGradleVersion.trim(), fileSystem)
    cliArguments.performCleanup = cliOptionCleanup

    if (gradleDistributionDirUrl) {
      cliArguments.gradleDistributionSiteUrl = gradleDistributionDirUrl.trim()
      cliArguments.gradleDistributionSiteUrl = cliArguments.gradleDistributionSiteUrl.endsWith("/") ? cliArguments.gradleDistributionSiteUrl : cliArguments.gradleDistributionSiteUrl + "/"
    }

    if (downloadDir) {
      cliArguments.downloadTargetDir = new File(downloadDir.trim()).absolutePath
    }

    if (repackDir) {
      cliArguments.gradleApiDirName = new File(repackDir.trim()).absolutePath
    }

    return cliArguments
  }

  private File fetchGradleDistributionZipFile(GradleSourceRepackCliArguments cliArguments, GradleDownloader gradleDownloader) {
    GradleDownloaderInfo gradleDownloaderZipInfo = cliArguments.toGradleDownloaderInfoForDistributionZip()

    File gradleDistributionZipFile = new File(gradleDownloaderZipInfo.downloadTargetFileAbsolutePath)
    if (gradleDistributionZipFile.exists()) {
      log.debug("Using already existing Gradle distribution ZIP file '{}'.", gradleDistributionZipFile.absolutePath)
    }
    else {
      log.debug("Starting download of Gradle distribution ZIP file.")
      gradleDistributionZipFile = gradleDownloader.download(gradleDownloaderZipInfo)
    }

    return gradleDistributionZipFile
  }

  private File fetchGradleDistributionZipSha256File(GradleSourceRepackCliArguments cliArguments, GradleDownloader gradleDownloader) {
    GradleDownloaderInfo gradleDownloaderZipSha256Info = cliArguments.toGradleDownloaderInfoForDistributionZipSha256File()

    File gradleDistributionZipSha256File = new File(gradleDownloaderZipSha256Info.downloadTargetFileAbsolutePath)
    if (gradleDistributionZipSha256File.exists()) {
      log.debug("Using already existing Gradle distribution's SHA-256 file '{}'.", gradleDistributionZipSha256File.absolutePath)
    }
    else {
      log.debug("Starting download of Gradle distribution's SHA-256 file.")
      gradleDistributionZipSha256File = gradleDownloader.download(gradleDownloaderZipSha256Info)
    }

    return gradleDistributionZipSha256File
  }

  private void cleanDownloadedFiles(List<File> fileListToDelete) {
    log.debug("Deleting downloaded files: {}", fileListToDelete)
    fileListToDelete.each((File file) -> file.delete())
  }

  @SuppressWarnings("CodeNarc.Println")
  private void printlnOnConsole(String message) {
    println message
  }
}
