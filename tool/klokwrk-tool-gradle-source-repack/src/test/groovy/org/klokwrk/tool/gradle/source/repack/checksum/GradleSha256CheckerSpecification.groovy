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
package org.klokwrk.tool.gradle.source.repack.checksum

import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import spock.lang.Specification

class GradleSha256CheckerSpecification extends Specification {
  void "should match for provided valid sha-256 checksum"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip.sha256").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    gradleSha256CheckInfo.isMatch()
  }

  void "should fail for provided invalid sha-256 checksum"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip.sha256-invalid").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    !gradleSha256CheckInfo.isMatch()
  }
}
