/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
