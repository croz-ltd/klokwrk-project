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
package org.klokwrk.tool.gradle.source.repack.checksum

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import org.slf4j.LoggerFactory
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

  // Demo for improving code coverage with Groovy @Slf4j annotation and different logging levels.
  // Groovy @Slf4j annotation generates the most performant code for logging. Unfortunatelly, as code is generated inline, annotation produces several branches which are probably
  // not covered by tests. This test provides a simple example of using already existing test and running it with different logging levels.
  void "should match for provided valid sha-256 checksum with different logging levels"() {
    given:
    Logger logger = LoggerFactory.getLogger("org.klokwrk.tool.gradle.source.repack.checksum.GradleSha256Checker") as Logger
    logger.level = loggerLevel

    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip.sha256").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    gradleSha256CheckInfo.isMatch()

    cleanup:
    logger.level = Level.WARN

    where:
    loggerLevel | _
    Level.WARN  | _
    Level.DEBUG | _
  }

  void "should fail for provided invalid sha-256 checksum"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionSha256File = new File(loader.getResource("classpath:testFiles/invalid_slim-gradle-6.7.1-all.zip.sha256").get().file)
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    when:
    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(testSlimGradleDistributionSha256File, testSlimGradleDistributionFile)

    then:
    !gradleSha256CheckInfo.isMatch()
  }
}
