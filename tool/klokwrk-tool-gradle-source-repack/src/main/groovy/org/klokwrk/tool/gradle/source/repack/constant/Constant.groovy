package org.klokwrk.tool.gradle.source.repack.constant

import groovy.transform.CompileStatic

/**
 * Shared common constants.
 * <p/>
 * Any part of application can use these.
 */
@CompileStatic
class Constant {
  /**
   * Default Gradle distribution type.
   * <p/>
   * Used for calculating Gradle distribution archive name for download.
   */
  static final String GRADLE_DISTRIBUTION_TYPE_DEFAULT = "all"

  /**
   * Default extension of downloadable Gradle distribution archive name.
   */
  static final String GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT = ".zip"

  /**
   * Default URL prefix to use for downloading Gradle distribution archive.
   */
  static final String GRADLE_DISTRIBUTION_SITE_URL_DEFAULT = "https://services.gradle.org/distributions/"
}
