package org.klokwrk.tool.gradle.source.repack

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import org.klokwrk.tool.gradle.source.repack.testutil.WireMockUtil
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
    outputString.contains("Usage: klokwrk-tool-gradle-source-repack")
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
    File downloadDir = new File(System.getProperty("java.io.tmpdir"))
    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    File repackDir = new File(System.getProperty("java.io.tmpdir"))
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    String[] args = [
        "--loggingLevels=org.klokwrk.tool.gradle.source.repack=DEBUG", "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }",
        "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.out = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String outputString = byteArrayOutputStream

    then:
    outputString.readLines()[0] ==~ /.*DEBUG.*o.k.t.g.s.r.GradleSourceRepackCommand.*-.*Started.*/
  }

  void "should work as expected with cleanup"() {
    given:
    File downloadDir = new File(System.getProperty("java.io.tmpdir"))
    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    File repackDir = new File(System.getProperty("java.io.tmpdir"))
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    String[] args = [
        "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }", "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)

    then:
    repackedSourceArchiveFile.exists()
    repackedSourceArchiveFile.size() > 0

    cleanup:
    repackedSourceArchiveFile.delete()
    !downloadedGradleDistributionFile.exists()
    !downloadedGradleDistributionSha256File.exists()
  }

  void "should work as expected without cleanup"() {
    given:
    File downloadDir = new File(System.getProperty("java.io.tmpdir"))
    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    File repackDir = new File(System.getProperty("java.io.tmpdir"))
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    String[] args = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }", "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)

    then:
    repackedSourceArchiveFile.exists()
    repackedSourceArchiveFile.size() > 0
    downloadedGradleDistributionFile.exists()
    downloadedGradleDistributionSha256File.exists()

    cleanup:
    repackedSourceArchiveFile.delete()
  }

  void "should work with already exiting downloaded files"() {
    given:
    File downloadDir = new File(System.getProperty("java.io.tmpdir"))
    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256", "gradle-6.7.1-all.zip.sha256")

    File repackDir = new File(System.getProperty("java.io.tmpdir"))
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    // first run to download files without cleaning up
    String[] args = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }", "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)

    // Second run works on already existing files
    args = [
        "--cleanup=true", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }", "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)

    then:
    repackedSourceArchiveFile.exists()
    repackedSourceArchiveFile.size() > 0

    cleanup:
    repackedSourceArchiveFile.delete()
  }

  void "should fail when SHA-256 does not match"() {
    given:
    File downloadDir = new File(System.getProperty("java.io.tmpdir"))
    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip", "gradle-6.7.1-all.zip")
    WireMockUtil.configureWireMockForGradleDistributionFile(wireMockServer, "slim-gradle-6.7.1-all.zip.sha256-invalid", "gradle-6.7.1-all.zip.sha256")

    File repackDir = new File(System.getProperty("java.io.tmpdir"))
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    String[] args = [
        "--cleanup=false", "--gradle-distribution-dir-url=${ wireMockServer.baseUrl() }", "--download-dir=${ downloadDir.absolutePath }", "--repack-dir=${ repackDir.absolutePath }", "6.7.1"
    ] as String[]

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.err = new PrintStream(byteArrayOutputStream)

    when:
    PicocliRunner.run(GradleSourceRepackCommand, applicationContext, args)
    String errorOutputString = byteArrayOutputStream

    then:
    errorOutputString.contains("java.lang.IllegalStateException: SHA-256 does not match")
  }
}
