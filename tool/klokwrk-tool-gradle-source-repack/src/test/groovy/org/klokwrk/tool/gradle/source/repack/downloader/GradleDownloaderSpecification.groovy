package org.klokwrk.tool.gradle.source.repack.downloader

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.klokwrk.tool.gradle.source.repack.constant.Constant
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.head
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching

class GradleDownloaderSpecification extends Specification {
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

  void "should fail when first HEAD request returns erroneous HTTP status code"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(aResponse().withStatus(404))
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("erroneous HTTP status code: [Not Found(404)]. Cannot continue.")
    illegalStateException.cause instanceof HttpClientResponseException
  }

  void "should fail when first HEAD request returns unexpected HTTP status code"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(aResponse().withStatus(201))
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("unexpected HTTP response status: [Created(201)]. Expected HTTP response statuses are [Ok(200), Moved Permanently(301)]. Cannot continue.")
    illegalStateException.cause == null
  }

  void "should fail when first HEAD request returns unexpected content length"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    // No content length was set
            )
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("not returned content length. Cannot continue.")
    illegalStateException.cause == null
  }

  void "should fail when redirected HEAD request returns erroneous HTTP status code"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "${ wireMockServer.baseUrl() }/newLocation/gradle-6.7.1-all.zip")
            )
    )

    wireMockServer.stubFor(
        head(urlMatching("/newLocation/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(404)
            )
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("erroneous HTTP status code: [Not Found(404)]. Cannot continue.")
    illegalStateException.cause instanceof HttpClientResponseException
  }

  void "should fail when redirected HEAD request returns unexpected HTTP status code"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "${ wireMockServer.baseUrl() }/newLocation/gradle-6.7.1-all.zip")
            )
    )

    wireMockServer.stubFor(
        head(urlMatching("/newLocation/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(201)
            )
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("unexpected HTTP response status: [Created(201)]. Expected HTTP response status is [Ok(200)]. Cannot continue.")
    illegalStateException.cause == null
  }

  void "should fail when redirected HEAD request returns unexpected content length"() {
    given:
    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "${ wireMockServer.baseUrl() }/newLocation/gradle-6.7.1-all.zip")
            )
    )

    wireMockServer.stubFor(
        head(urlMatching("/newLocation/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    // No content length was set
            )
    )

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", System.getProperty("user.dir")
    )

    when:
    gradleDownloader.download(gradleDownloaderInfo)

    then:
    IllegalStateException illegalStateException = thrown()
    illegalStateException.message.contains("not returned content length. Cannot continue.")
    illegalStateException.cause == null
  }

  void "should work when HTTP redirect does not happen"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)
    Long testSlimGradleDistributionFileSizeInBytes = testSlimGradleDistributionFile.size()

    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Length", testSlimGradleDistributionFileSizeInBytes.toString())
            )
    )

    wireMockServer.stubFor(
        get(urlMatching("/gradle-6.7.1-all.zip"))
        .willReturn(
            aResponse()
                .withStatus(200)
                .withHeader("Content-Length", testSlimGradleDistributionFileSizeInBytes.toString())
                .withBody(testSlimGradleDistributionFile.bytes)
        )
    )

    String downloadTargetDir = "${ System.getProperty("user.dir") }/build/_testrun/${ UUID.randomUUID() }/"
    Files.createDirectories(Paths.get(downloadTargetDir))

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", downloadTargetDir
    )

    when:
    File gradleDistributionDownloadFile = gradleDownloader.download(gradleDownloaderInfo)

    then:
    gradleDistributionDownloadFile.exists()
    gradleDistributionDownloadFile.name == "gradle-6.7.1-all.zip"
    gradleDistributionDownloadFile.size() == testSlimGradleDistributionFileSizeInBytes

    cleanup:
    gradleDistributionDownloadFile.delete()
    new File(downloadTargetDir).delete()
  }

  void "should work with HTTP redirect"() {
    given:
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testSlimGradleDistributionFile = new File(loader.getResource("classpath:testFiles/slim-gradle-6.7.1-all.zip").get().file)
    Long testSlimGradleDistributionFileSizeInBytes = testSlimGradleDistributionFile.size()

    wireMockServer.stubFor(
        head(urlMatching("/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "${ wireMockServer.baseUrl() }/newLocation/gradle-6.7.1-all.zip")
            )
    )

    wireMockServer.stubFor(
        head(urlMatching("/newLocation/gradle-6.7.1-all.zip"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Length", testSlimGradleDistributionFileSizeInBytes.toString())
            )
    )

    wireMockServer.stubFor(
        get(urlMatching("/newLocation/gradle-6.7.1-all.zip"))
        .willReturn(
            aResponse()
                .withStatus(200)
                .withHeader("Content-Length", testSlimGradleDistributionFileSizeInBytes.toString())
                .withBody(testSlimGradleDistributionFile.bytes)
        )
    )

    String downloadTargetDir = "${ System.getProperty("user.dir") }/build/_testrun/${ UUID.randomUUID() }/"
    Files.createDirectories(Paths.get(downloadTargetDir))

    GradleDownloader gradleDownloader = applicationContext.getBean(GradleDownloader)
    GradleDownloaderInfo gradleDownloaderInfo = new GradleDownloaderInfo(
        "6.7.1", Constant.GRADLE_DISTRIBUTION_TYPE_DEFAULT, Constant.GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT, "${ wireMockServer.baseUrl() }/", downloadTargetDir
    )

    when:
    File gradleDistributionDownloadFile = gradleDownloader.download(gradleDownloaderInfo)

    then:
    gradleDistributionDownloadFile.exists()
    gradleDistributionDownloadFile.name == "gradle-6.7.1-all.zip"
    gradleDistributionDownloadFile.size() == testSlimGradleDistributionFileSizeInBytes

    cleanup:
    gradleDistributionDownloadFile.delete()
    new File(downloadTargetDir).delete()
  }
}
