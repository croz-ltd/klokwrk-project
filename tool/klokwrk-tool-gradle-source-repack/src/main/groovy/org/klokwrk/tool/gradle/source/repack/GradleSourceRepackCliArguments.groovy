package org.klokwrk.tool.gradle.source.repack

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Encapsulates supported CLI options.
 * <p/>
 * After picocli finishes with parsing configured CLI options, it is expected to supply them into an instance of this class for easier communication with other interested classes.
 * <p/>
 * Only required option is Gradle version. All other options are initialized to defaults that can be changed via setters.
 */
@ToString(includeNames = true)
@CompileStatic
class GradleSourceRepackCliArguments {
  private final String gradleVersion

  /**
   * Indicates if downloaded resources should be deleted after repacking finishes.
   */
  Boolean performCleanup

  GradleSourceRepackCliArguments(String gradleVersion) {
    this.gradleVersion = gradleVersion

    this.performCleanup = true
  }

  /**
   * Gradle version whose sources will be downloaded and repacked.
   */
  String getGradleVersion() {
    return gradleVersion
  }
}
