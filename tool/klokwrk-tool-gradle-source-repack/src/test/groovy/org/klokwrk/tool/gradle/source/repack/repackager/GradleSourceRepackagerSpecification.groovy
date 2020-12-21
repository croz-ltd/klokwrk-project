/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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

import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class GradleSourceRepackagerSpecification extends Specification {
  void "should fail when Gradle distribution does not exist"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)
    String repackedZipFileDirectoryPath = "${ System.getProperty("user.dir") }"
    String repackedZipFilePath = "${ repackedZipFileDirectoryPath }slim-gradle-api-6.7.1-sources.zip"

    GradleSourceRepackagerInfo gradleSourceRepackagerInfo =
        new GradleSourceRepackagerInfo(testSlimGradleDistributionFile.absolutePath + "non-existing", "6.7.1", repackedZipFilePath, repackedZipFileDirectoryPath)

    when:
    GradleSourceRepackager.repackGradleSource(gradleSourceRepackagerInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.startsWith("Source Gradle distribution does not exist:")
  }

  void "should fail when target directory for repacking does not exist"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)
    String repackedZipFileDirectoryPath = "${ System.getProperty("user.dir") }/build/non-existing/"
    String repackedZipFilePath = "${ repackedZipFileDirectoryPath }slim-gradle-api-6.7.1-sources.zip"

    GradleSourceRepackagerInfo gradleSourceRepackagerInfo = new GradleSourceRepackagerInfo(testSlimGradleDistributionFile.absolutePath, "6.7.1", repackedZipFilePath, repackedZipFileDirectoryPath)

    when:
    GradleSourceRepackager.repackGradleSource(gradleSourceRepackagerInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.startsWith("Target directory for repacking does not exist:")
  }

  void "should repack Gradle source archive without duplicates skipping in target"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)

    String repackedZipFileDirectoryPath = "${ System.getProperty("user.dir") }/build/_testrun/${ UUID.randomUUID() }/"
    Files.createDirectories(Paths.get(repackedZipFileDirectoryPath))

    String repackedZipFilePath = "${ repackedZipFileDirectoryPath }slim-gradle-api-6.7.1-sources.zip"

    GradleSourceRepackagerInfo gradleSourceRepackagerInfo = new GradleSourceRepackagerInfo(testSlimGradleDistributionFile.absolutePath, "6.7.1", repackedZipFilePath, repackedZipFileDirectoryPath)

    when:
    GradleSourceRepackager.repackGradleSource(gradleSourceRepackagerInfo)
    File repackedZipFile = new File(repackedZipFilePath)

    then:
    repackedZipFile.exists()
    repackedZipFilePath.size() > 0

    cleanup:
    new File(repackedZipFilePath).delete()
    new File(repackedZipFileDirectoryPath).delete()
  }

  void "should repack Gradle source archive with duplicates skipping in target"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-with-duplicates-gradle-6.7.1-all.zip").get().file)

    String repackedZipFileDirectoryPath = "${ System.getProperty("user.dir") }/build/_testrun/${ UUID.randomUUID() }/"
    Files.createDirectories(Paths.get(repackedZipFileDirectoryPath))

    String repackedZipFilePath = "${ repackedZipFileDirectoryPath }slim-gradle-api-6.7.1-sources.zip"

    GradleSourceRepackagerInfo gradleSourceRepackagerInfo = new GradleSourceRepackagerInfo(testSlimGradleDistributionFile.absolutePath, "6.7.1", repackedZipFilePath, repackedZipFileDirectoryPath)

    when:
    GradleSourceRepackager.repackGradleSource(gradleSourceRepackagerInfo)
    File repackedZipFile = new File(repackedZipFilePath)

    then:
    repackedZipFile.exists()
    repackedZipFilePath.size() > 0

    cleanup:
    new File(repackedZipFilePath).delete()
    new File(repackedZipFileDirectoryPath).delete()
  }
}
