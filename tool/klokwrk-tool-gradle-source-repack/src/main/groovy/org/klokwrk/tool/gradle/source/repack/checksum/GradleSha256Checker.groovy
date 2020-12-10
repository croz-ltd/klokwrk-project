package org.klokwrk.tool.gradle.source.repack.checksum

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Checks SHA-256 checksum of Gradle distribution ZIP file.
 */
@CompileStatic
class GradleSha256Checker {
  private static final Logger log = LoggerFactory.getLogger(GradleSha256Checker)

  /**
   * Calculates SHA-256 checksum of Gradle distribution ZIP file and compares it to the provided file containing SHA-256 hex encoded checksum.
   */
  static GradleSha256CheckInfo checkSha256(File gradleDistributionZipSha256File, File gradleDistributionZipFile) {
    String fetchedSha256 = gradleDistributionZipSha256File.readLines()[0]
    log.debug("Fetched SHA-256   : {}", fetchedSha256)

    String calculatedSha256 = ChecksumCalculator.calculateAsHexEncodedString(gradleDistributionZipFile, "SHA-256")
    log.debug("Calculated SHA-256: {}", calculatedSha256)

    return new GradleSha256CheckInfo(fetchedSha256, calculatedSha256)
  }
}
