package org.klokwrk.tool.gradle.source.repack.downloader

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Encapsulates data needed for downloading Gradle distribution or helper files.
 */
@Immutable(includeNames = true)
@CompileStatic
class GradleDownloaderInfo {
  String gradleVersion
  String gradleDistributionType
  String gradleDistributionFileExtension
  String downloadSiteUrl

  /**
   * Directory where downloaded item will be placed.
   */
  String downloadTargetDir

  String getGradleDistributionFileName() {
    return "gradle-${ gradleVersion }-${ gradleDistributionType }${ gradleDistributionFileExtension }"
  }

  /**
   * Full URL of an item to download, including site URL and a file name of an item.
   */
  String getFullDownloadUrl() {
    return "${ downloadSiteUrl }${ gradleDistributionFileName }"
  }

  /**
   * Absolute path of downloaded item.
   */
  String getDownloadTargetFileAbsolutePath() {
    String fileSeparator = System.getProperty("file.separator")
    return "${ downloadTargetDir }${ fileSeparator }${ gradleDistributionFileName }"
  }
}
