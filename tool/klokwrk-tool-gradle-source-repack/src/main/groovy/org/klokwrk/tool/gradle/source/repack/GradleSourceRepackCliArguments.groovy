/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
import groovy.transform.ToString
import org.klokwrk.tool.gradle.source.repack.constant.Constant
import org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloaderInfo
import org.klokwrk.tool.gradle.source.repack.repackager.GradleSourceRepackagerInfo

import java.nio.file.FileSystems
import java.nio.file.Files

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
  /**
   * Gradle version whose sources will be downloaded and repacked.
   */
  String gradleVersion

  /**
   * Indicates if downloaded resources should be deleted after repacking finishes.
   */
  Boolean performCleanup

  /**
   * Gradle distribution type to use.
   * <p/>
   * Used for calculating Gradle distribution archive name for download. It defaults to {@link Constant#GRADLE_DISTRIBUTION_TYPE_DEFAULT}.
   */
  String gradleDistributionType

  /**
   * Extension of downloadable Gradle distribution archive name.
   * <p/>
   * It defaults to {@link Constant#GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT}.
   */
  String gradleDistributionFileExtension

  /**
   * URL of the site where downloadable Gradle distribution archive resides.
   * <p/>
   * It defaults to {@link Constant#GRADLE_DISTRIBUTION_SITE_URL_DEFAULT}
   */
  String gradleDistributionSiteUrl

  /**
   * Directory into which Gradle distribution archive will be downloaded.
   * <p/>
   * It defaults to current working directory, meaning a directory from which command is started.
   */
  String downloadTargetDir

  /**
   * The file name of the archive into which Gradle sources will be repacked.
   * <p/>
   * It defaults to {@code gradle-api-${ this.gradleVersion }-sources.jar }
   */
  String gradleApiSourcesFileName

  /**
   * The directory where repacked archive will be placed.
   * <p/>
   * If directory {@code ~/.gradle/caches/${ this.gradleVersion }/generated-gradle-jars} exists, it will be used. Otherwise, {@code downloadTargetDir} is used.
   */
  String gradleApiDirName

  GradleSourceRepackCliArguments(String gradleVersion) {
    this.gradleVersion = gradleVersion
    this.gradleDistributionType = Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT
    this.gradleDistributionFileExtension = Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT
    this.gradleDistributionSiteUrl = Constant.GRADLE_DISTRIBUTION_SITE_URL_DEFAULT
    this.downloadTargetDir = System.getProperty("user.dir")

    this.gradleApiSourcesFileName = "gradle-api-${ this.gradleVersion }-sources.jar"

    // If default target directory for repacking does not exist, repack into a directory where gradle distribution was downloaded.
    String gradleApiDirNameToCheck = "${ System.getProperty("user.home") }/.gradle/caches/${ this.gradleVersion }/generated-gradle-jars"
    if (Files.exists(FileSystems.default.getPath(gradleApiDirNameToCheck))) {
      this.gradleApiDirName = gradleApiDirNameToCheck
    }
    else {
      this.gradleApiDirName = downloadTargetDir
    }

    this.performCleanup = true
  }

  String getGradleApiSourcesFilePath() {
    return getGradleApiDirName() + "/" + getGradleApiSourcesFileName()
  }

  /**
   * Factory method for creating {@code GradleDownloaderInfo} for main Gradle distribution ZIP file.
   */
  GradleDownloaderInfo toGradleDownloaderInfoForDistributionZip() {
    return new GradleDownloaderInfo(gradleVersion, gradleDistributionType, gradleDistributionFileExtension, gradleDistributionSiteUrl, downloadTargetDir)
  }

  /**
   * Factory method for creating {@code GradleDownloaderInfo} for supportive SHA-256 file of corresponding Gradle distribution ZIP file.
   */
  GradleDownloaderInfo toGradleDownloaderInfoForDistributionZipSha256File() {
    return new GradleDownloaderInfo(gradleVersion, gradleDistributionType, "${ gradleDistributionFileExtension }.sha256", gradleDistributionSiteUrl, downloadTargetDir)
  }

  /**
   * Factory method for creating {@code GradleSourceRepackagerInfo}.
   */
  GradleSourceRepackagerInfo toGradleSourceRepackagerInfo(String gradleDistributionZipFilePath) {
    return new GradleSourceRepackagerInfo(gradleDistributionZipFilePath, gradleVersion, gradleApiSourcesFilePath, gradleApiDirName)
  }
}
