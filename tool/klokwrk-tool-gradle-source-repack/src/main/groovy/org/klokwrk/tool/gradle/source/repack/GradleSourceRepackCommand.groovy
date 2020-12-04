package org.klokwrk.tool.gradle.source.repack

import groovy.transform.CompileStatic
import io.micronaut.configuration.picocli.PicocliRunner
import org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider
import org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader
import org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloaderInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility
import picocli.CommandLine.Model
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import javax.inject.Inject
import java.util.regex.Pattern

@SuppressWarnings("JavaIoPackageAccess")
@Command(
    name = "klokwrk-tool-gradle-source-repack",
    description = "Downloads Gradle source distribution and repackages it in a JAR suitable to use for debugging Gradle internals in IDEA.",
    mixinStandardHelpOptions = true,
    versionProvider = PropertiesVersionProvider
)
@CompileStatic
class GradleSourceRepackCommand implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(GradleSourceRepackCommand)

  private static final String GRADLE_VERSION_REGEX_FORMAT = /([2-9]\d*){1}(\.\d+){1}(\.\d+)?(-([a-zA-Z1-9]+))?/
  private static final Pattern GRADLE_VERSION_REGEX_PATTERN = ~GRADLE_VERSION_REGEX_FORMAT

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

  @Inject
  GradleDownloader gradleDownloader

  @Override
  void run() {
    log.debug("Started.")

    GradleSourceRepackCliArguments cliArguments = new GradleSourceRepackCliArguments(cliParameterGradleVersion)
    cliArguments.performCleanup = cliOptionCleanup
    log.debug("cliArguments: {}", cliArguments)

    File gradleDistributionZipFile = fetchGradleDistributionZipFile(cliArguments, gradleDownloader)

    log.debug("Finished.")
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
}
