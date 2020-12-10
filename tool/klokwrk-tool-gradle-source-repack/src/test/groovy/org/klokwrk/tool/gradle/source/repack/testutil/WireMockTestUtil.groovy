package org.klokwrk.tool.gradle.source.repack.testutil

import com.github.tomakehurst.wiremock.WireMockServer
import groovy.transform.CompileStatic
import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.head
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching

@CompileStatic
class WireMockTestUtil {
  static void configureWireMockForGradleDistributionFile(WireMockServer wireMockServer, String realResourceFileName, String downloadFileName) {
    ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader).get()
    File testGradleDistributionFile = new File(loader.getResource("classpath:testFiles/${ realResourceFileName }").get().file)
    Long testGradleDistributionFileSizeInBytes = testGradleDistributionFile.size()

    wireMockServer.stubFor(
        head(urlMatching("/${ downloadFileName }"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "${ wireMockServer.baseUrl() }/newLocation/${ downloadFileName }")
            )
    )

    wireMockServer.stubFor(
        head(urlMatching("/newLocation/${ downloadFileName }"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Length", testGradleDistributionFileSizeInBytes.toString())
            )
    )

    wireMockServer.stubFor(
        get(urlMatching("/newLocation/${ downloadFileName }"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Length", testGradleDistributionFileSizeInBytes.toString())
                    .withBody(testGradleDistributionFile.bytes)
            )
    )
  }
}
