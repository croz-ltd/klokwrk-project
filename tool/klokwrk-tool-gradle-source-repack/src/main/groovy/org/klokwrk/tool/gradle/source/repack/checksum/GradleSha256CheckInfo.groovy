package org.klokwrk.tool.gradle.source.repack.checksum

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Encapsulates data needed for reporting result of SHA-256 check on Gradle distribution ZIP file.
 */
@Immutable(includeNames = true)
@CompileStatic
class GradleSha256CheckInfo {

  /**
   * SHA-256 value read from supportive SHA-256 file of corresponding Gradle distribution ZIP file.
   */
  String fetchedSha256

  /**
   * Calculated SHA-256 of Gradle distribution file.
   */
  String calculatedSha256

  /**
   * Returns true when fetched and calculated SHA-256 hashes match.
   */
  Boolean isMatch() {
    return fetchedSha256 == calculatedSha256
  }
}
