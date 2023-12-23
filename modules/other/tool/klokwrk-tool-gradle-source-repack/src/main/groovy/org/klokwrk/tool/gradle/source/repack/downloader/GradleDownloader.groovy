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
package org.klokwrk.tool.gradle.source.repack.downloader

import groovy.transform.CompileStatic
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.reactor.http.client.ReactorStreamingHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.inject.Singleton

/**
 * Singleton service that downloads Gradle distribution files (typically {@code *.zip} or {@code *.zip.sha256}).
 */
@SuppressWarnings("CodeNarc.JavaIoPackageAccess")
@Singleton
@CompileStatic
class GradleDownloader {
  private static final Logger log = LoggerFactory.getLogger(GradleDownloader)

  ReactorStreamingHttpClient streamingHttpClient
  HttpClient headOnlyHttpClient

  /**
   * Constructs GradleDownloader with two http client dependencies.
   * <p/>
   * Http client {@code streamingHttpClient} is used for downloading potentially large Gradle distribution files. Http client {@code headOnlyHttpClient} is used only for executing HEAD requests
   * used for getting an info about the size of there resource to be downloaded with {@code streamingHttpClient}.
   */
  GradleDownloader(@Client ReactorStreamingHttpClient streamingHttpClient, @Client("head-only") HttpClient headOnlyHttpClient) {
    this.streamingHttpClient = streamingHttpClient
    this.headOnlyHttpClient = headOnlyHttpClient
  }

  /**
   * Downloads Gradle distribution files (typically *.zip or *.zip.sha256) based on provided {@link GradleDownloaderInfo} specification.
   */
  File download(GradleDownloaderInfo gradleDownloaderInfo) {
    log.debug("Starting download with following gradleDownloaderInfo: {}", gradleDownloaderInfo)

    Tuple2 realDownloadUrlAndContentLengthTuple = calculateRealDownloadUrlAndContentLength(gradleDownloaderInfo)
    String realDownloadUrl = realDownloadUrlAndContentLengthTuple.v1
    String contentLength = realDownloadUrlAndContentLengthTuple.v2

    log.info("Downloading: '{}' ==> '{}'.", realDownloadUrl, gradleDownloaderInfo.downloadTargetFileAbsolutePath)
    log.debug("Content-Length for '{}': {} ({} MiB).", realDownloadUrl, contentLength, contentLength.toLong() / (1024 * 1024))

    new BufferedOutputStream(new FileOutputStream(gradleDownloaderInfo.downloadTargetFileAbsolutePath), 1024 * 1024).withCloseable { BufferedOutputStream fileOutputStream ->
      Long downloadedBytesCount = 0
      println "Downloading '${ realDownloadUrl }':" // codenarc-disable-line Println
      streamingHttpClient.exchangeStream(HttpRequest.GET(realDownloadUrl).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE))
                         .map({ HttpResponse<ByteBuffer<?>> byteBufferHttpResponse ->
                           // TODO dmurat: Change back getBody() to body when Groovy 4.0.18 is released (https://issues.apache.org/jira/browse/GROOVY-11257), and remove CodeNarc comment
                           byte[] byteArray = byteBufferHttpResponse.getBody().orElseThrow({ new NoSuchElementException("No value present") }).toByteArray() // codenarc-disable UnnecessaryGetter

                           downloadedBytesCount += byteArray.length
                           printOutDownloadProgress(downloadedBytesCount, contentLength)

                           return byteArray
                         })
                         .doOnNext({ byte[] byteArray -> fileOutputStream.write(byteArray) })
                         .doOnComplete({ printlnOutNewline() })
                         .blockLast()
    }

    return new File(gradleDownloaderInfo.downloadTargetFileAbsolutePath)
  }

  protected Tuple2<String, String> calculateRealDownloadUrlAndContentLength(GradleDownloaderInfo gradleDownloaderInfo) {
    HttpResponse<?> headResponse = fetchFirstHeadResponse(gradleDownloaderInfo.fullDownloadUrl)

    String contentLength
    String realDownloadUrl = gradleDownloaderInfo.fullDownloadUrl
    if (HttpStatus.MOVED_PERMANENTLY == headResponse.status) {
      String newLocationUrl = headResponse.header(HttpHeaders.LOCATION)
      log.debug("HTTP status for '{}': {}({}). New location is '{}'.", gradleDownloaderInfo.downloadSiteUrl, HttpStatus.MOVED_PERMANENTLY.reason, HttpStatus.MOVED_PERMANENTLY.code, newLocationUrl)

      Tuple2<String, HttpResponse<?>> newLocationUrlAndHeadResponseTuple = fetchRedirectedHeadResponse(newLocationUrl)
      contentLength = newLocationUrlAndHeadResponseTuple.v2.header(HttpHeaders.CONTENT_LENGTH) ?: "-1"
      realDownloadUrl = newLocationUrlAndHeadResponseTuple.v1
    }
    else {
      contentLength = headResponse.header(HttpHeaders.CONTENT_LENGTH) ?: "-1"
    }

    if (contentLength == "-1") {
      String message = "HEAD request for '${ realDownloadUrl }' did not returned content length. Cannot continue."
      throw new IllegalStateException(message)
    }

    return Tuple.tuple(realDownloadUrl, contentLength)
  }

  protected HttpResponse<?> fetchFirstHeadResponse(String url) {
    HttpResponse<?> headResponse = fetchGeneralHeadResponse(url)

    List<HttpStatus> expectedHttpStatusList = [HttpStatus.OK, HttpStatus.MOVED_PERMANENTLY]
    if (headResponse.status !in expectedHttpStatusList) {
      String message = "HEAD request for '${ url }' returned unexpected HTTP response status: [${ headResponse.status.reason }(${ headResponse.status.code })]. " +
                       "Expected HTTP response statuses are [${ expectedHttpStatusList.collect({ "${ it.reason }(${ it.code })" }).join(", ") }]. Cannot continue."
      throw new IllegalStateException(message)
    }

    return headResponse
  }

  protected Tuple2<String, HttpResponse<?>> fetchRedirectedHeadResponse(String url) {
    HttpResponse<?> secondHeadResponse = fetchGeneralHeadResponse(url)
    String newLocationUrl = url

    if (secondHeadResponse.status == HttpStatus.FOUND) {
      newLocationUrl = secondHeadResponse.header(HttpHeaders.LOCATION)
      log.debug("HTTP status for '{}': {}({}). New location is '{}'.", url, HttpStatus.FOUND.reason, HttpStatus.FOUND.code, newLocationUrl) // codenarc-disable-line DuplicateStringLiteral
      secondHeadResponse = fetchGeneralHeadResponse(newLocationUrl)
    }

    if (secondHeadResponse.status.code != HttpStatus.OK.code) {
      String message = "HEAD request for '${ url }' returned unexpected HTTP response status: [${ secondHeadResponse.status.reason }(${ secondHeadResponse.status.code })]. " +
                       "Expected HTTP response status is [${ HttpStatus.OK.reason }(${ HttpStatus.OK.code })]. Cannot continue."
      throw new IllegalStateException(message)
    }

    return Tuple.tuple(newLocationUrl, secondHeadResponse)
  }

  protected HttpResponse<?> fetchGeneralHeadResponse(String url) {
    HttpResponse<?> generalHeadResponse
    try {
      generalHeadResponse = headOnlyHttpClient.toBlocking().exchange(HttpRequest.HEAD(url))
    }
    catch (HttpClientResponseException hcre) {
      String message = "HEAD request for '${ url }' returned erroneous HTTP status code: [${ hcre.status.reason }(${ hcre.status.code })]. Cannot continue."
      throw new IllegalStateException(message, hcre)
    }

    return generalHeadResponse
  }

  @SuppressWarnings("CodeNarc.Println")
  protected void printOutDownloadProgress(long downloadedBytesCount, String contentLength) {
    printf("\r%d%%", (downloadedBytesCount * 100 / contentLength.toLong()).toLong())
  }

  @SuppressWarnings("CodeNarc.Println")
  protected void printlnOutNewline() {
    println ""
  }
}
