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
import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * Encapsulates data needed for reporting result of SHA-256 check on Gradle distribution ZIP file.
 */
@ToString(includeNames = true) // TODO dmurat: Move annotation attribute into @Immutable when and if https://github.com/micronaut-projects/micronaut-core/issues/7220 gets fixed.
@Immutable
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
