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
package org.klokwrk.tool.gradle.source.repack.checksum

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Checks SHA-256 checksum of Gradle distribution ZIP file.
 */
@Slf4j
@CompileStatic
class GradleSha256Checker {
  /**
   * Calculates SHA-256 checksum of Gradle distribution ZIP file and compares it to the provided file containing SHA-256 hex encoded checksum.
   */
  static GradleSha256CheckInfo checkSha256(File gradleDistributionZipSha256File, File gradleDistributionZipFile) {
    String fetchedSha256 = gradleDistributionZipSha256File.readLines()[0]
    log.debug("Fetched SHA-256   : ${fetchedSha256}")

    String calculatedSha256 = ChecksumCalculator.calculateAsHexEncodedString(gradleDistributionZipFile, "SHA-256")
    log.debug("Calculated SHA-256: ${calculatedSha256}")

    return new GradleSha256CheckInfo(fetchedSha256, calculatedSha256)
  }
}
