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
package org.klokwrk.tool.gradle.source.repack

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import org.klokwrk.tool.gradle.source.repack.testutil.FileTestUtil
import org.klokwrk.tool.gradle.source.repack.testutil.WireMockTestUtil
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class GradleSourceRepackCommandSpecification extends Specification {

  @Shared
  @AutoCleanup
  ApplicationContext applicationContext = ApplicationContext.run(Environment.CLI, Environment.TEST)

  @Shared
  WireMockServer wireMockServer

  void setupSpec() {
    wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort())
    wireMockServer.start()
  }

  void cleanupSpec() {
    wireMockServer.resetAll()
    wireMockServer.stop()

    FileTestUtil.delete(new File("${ System.getProperty("user.dir") }/build/_testrun/"))
  }

  void setup() {
    wireMockServer.resetAll()
  }

  void "should display help message"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    String[] args = ["--help"] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    expect:
    outputString.contains("Usage:")
    outputString.contains("Downloads Gradle source distribution")
    outputString.trim().endsWith("Print version information and exit.")
  }

  void "should not display hidden options in help message"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    String[] args = ["--help"] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    expect:
    !outputString.contains("--gradle-distribution-dir-url")
    !outputString.contains("--download-dir")
    !outputString.contains("--repack-dir")
  }

  void "should display version message"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    String[] args = ["--version"] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    expect:
    outputString.contains("klokwrk-tool-gradle-source-repack")
  }

  void "should fail for invalid gradle version"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.err = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, [gradleVersion] as String[])
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("Invalid value '${ gradleVersion }' for parameter '<gradle-version>'.")

    where:
    gradleVersion | _
    "6"           | _
    "6."          | _
    "6.7."        | _
    "6.7.1."      | _
    "6.7.1.1"     | _
    "a"           | _
    "6.a"         | _
    "6.7.a"       | _
  }

  void "should fail for invalid loggingLevels option"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.err = new PrintStream(byteArrayOutputStream)

    String[] args = ["--loggingLevels=${ loggingLevels }", "6.7.1"] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("--loggingLevels option contains invalid configuration:")

    where:
    loggingLevels | _
    "ROOT=INFO,org.klokwrk.tool.gradle.source.repack-DEBUG"   | _
    "ROOT=INFO;org.klokwrk.tool.gradle.source.repack=DEBUG"   | _
    "ROOT=INFO-org.klokwrk.tool.gradle.source.repack=DEBUG"   | _
  }

  void "should work as expected for valid loggingLevels option"() {
    given:
    Map<String, File> testDirectoriesAndFiles = FileTestUtil.prepareDirectoriesAndFiles()
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    String[] args = [
        "--loggingLevels=org.klokwrk.tool.gradle.source.repack=DEBUG", "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    then:
    outputString.readLines()[0] ==~ /.*DEBUG.*o.k.t.g.s.r.GradleSourceRepackCommand.*-.*Started.*/
    testDirectoriesAndFiles.repackedSourceArchiveFile.exists()
  }

  void "should work as expected with cleanup"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    Map<String, File> testDirectoriesAndFiles = FileTestUtil.prepareDirectoriesAndFiles()
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    String[] args = [
        "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("gradle-api-6.7.1-sources.jar: 100%")
    testDirectoriesAndFiles.repackedSourceArchiveFile.exists()
    !testDirectoriesAndFiles.downloadedGradleDistributionFile.exists()
    !testDirectoriesAndFiles.downloadedGradleDistributionSha256File.exists()
  }

  void "should work as expected without cleanup"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    Map<String, File> testDirectoriesAndFiles = FileTestUtil.prepareDirectoriesAndFiles()
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    String[] args = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("gradle-api-6.7.1-sources.jar: 100%")
    testDirectoriesAndFiles.repackedSourceArchiveFile.exists()
    testDirectoriesAndFiles.downloadedGradleDistributionFile.exists()
    testDirectoriesAndFiles.downloadedGradleDistributionSha256File.exists()
  }

  void "should work with already exiting downloaded files"() {
    given:
    Map<String, File> testDirectoriesAndFiles = FileTestUtil.prepareDirectoriesAndFiles()
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    // first run to download files without cleaning up
    String[] firstRunArgs = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, firstRunArgs)

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    // Second run works on already existing files
    String[] secondRunArgs = [
        "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, secondRunArgs)
    String outputString = byteArrayOutputStream

    then:
    outputString.contains("gradle-api-6.7.1-sources.jar: 100%")
    testDirectoriesAndFiles.repackedSourceArchiveFile.exists()
    !testDirectoriesAndFiles.downloadedGradleDistributionFile.exists()
    !testDirectoriesAndFiles.downloadedGradleDistributionSha256File.exists()
  }

  void "should fail when SHA-256 does not match"() {
    given:
    Map<String, File> testDirectoriesAndFiles = FileTestUtil.prepareDirectoriesAndFiles()
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockTestUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256-invalid", "gradle-6.7.1-all.zip.sha256")

    String[] args = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }",
        "--download-dir=${ testDirectoriesAndFiles.downloadDir.absolutePath }", "--repack-dir=${ testDirectoriesAndFiles.repackDir.absolutePath }", "6.7.1"
    ] as String[]

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.err = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String errorOutputString = byteArrayOutputStream

    then:
    errorOutputString.contains("java.lang.IllegalStateException: SHA-256 does not match")
    !testDirectoriesAndFiles.repackedSourceArchiveFile.exists()
  }
}
