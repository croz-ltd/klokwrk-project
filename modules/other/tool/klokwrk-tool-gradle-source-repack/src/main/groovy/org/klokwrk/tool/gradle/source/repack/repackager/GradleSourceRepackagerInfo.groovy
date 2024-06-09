/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.tool.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * Encapsulates data needed for repackaging of Gradle sources from Gradle main distribution ZIP file.
 */
@ToString(includeNames = true) // Workaround for a bug with @Immutable annotation: https://github.com/micronaut-projects/micronaut-core/issues/7220
@Immutable
@CompileStatic
class GradleSourceRepackagerInfo {
  /**
   * Absolute path of Gradle distribution ZIP file from which original sources will be read.
   */
  String gradleDistributionZipFilePath

  /**
   * Gradle version.
   */
  String gradleVersion

  /**
   * Absolute path of an archive into which we will place repacked Gradle sources.
   * <p/>
   * It consists of archive file name prefixed with {@code gradleApiDirPath}.
   */
  String gradleApiSourcesFilePath

  /**
   * Absolute path of directory into which we will place an archive with repacked Gradle sources.
   * <p/>
   * By default it corresponds to the {@code generated-gradle-jars} subdirectory of directory where Gradle caches its distribution:
   * {@code System.getProperty("user.home") + "/.gradle/caches/" + this.gradleVersion + "/generated-gradle-jars"}. If that directory does not exists it falls back to the directory where Gradle
   * distribution is downloaded, which is current working directory by default.
   */
  String gradleApiDirPath

  /**
   * Path to the {@code src} directory inside of Gradle distribution archive (e.g. {@code gradle-6.7.1/src/}).
   */
  String getGradleDistributionSrcDirPath() {
    return "gradle-${ gradleVersion }/src/"
  }
}
