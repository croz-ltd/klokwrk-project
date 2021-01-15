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
package org.klokwrk.tool.gradle.source.repack.downloader

import groovy.transform.CompileStatic
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Singleton

/**
 * Singleton service that downloads Gradle distribution files (typically {@code *.zip} or {@code *.zip.sha256}).
 */
@SuppressWarnings("JavaIoPackageAccess")
@Singleton
@CompileStatic
class GradleDownloader {
  private static final Logger log = LoggerFactory.getLogger(GradleDownloader)

  RxStreamingHttpClient streamingHttpClient
  HttpClient headOnlyHttpClient

  /**
   * Constructs GradleDownloader with two http client dependencies.
   * <p/>
   * Http client {@code streamingHttpClient} is used for downloading potentially large Gradle distribution files. Http client {@code headOnlyHttpClient} is used only for executing HEAD requests
   * used for getting an info about the size of there resource to be downloaded with {@code streamingHttpClient}.
   */
  GradleDownloader(@Client RxStreamingHttpClient streamingHttpClient, @Client("head-only") HttpClient headOnlyHttpClient) {
    this.streamingHttpClient = streamingHttpClient
    this.headOnlyHttpClient = headOnlyHttpClient
  }

  /**
   * Downloads Gradle distribution files (typically *.zip or *.zip.sha256) based on provided {@link GradleDownloaderInfo} specification.
   */
  // TODO dmurat: revise Indentation warning suppression after analysis and potential bug report to CodeNarc.
  @SuppressWarnings("Indentation")
  File download(GradleDownloaderInfo gradleDownloaderInfo) {
    log.debug("Starting download with following gradleDownloaderInfo: {}", gradleDownloaderInfo)

    Tuple2 realDownloadUrlAndContentLengthTuple = calculateRealDownloadUrlAndContentLength(gradleDownloaderInfo)
    String realDownloadUrl = realDownloadUrlAndContentLengthTuple.v1
    String contentLength = realDownloadUrlAndContentLengthTuple.v2

    log.info("Downloading: '{}' ==> '{}'.", realDownloadUrl, gradleDownloaderInfo.downloadTargetFileAbsolutePath)
    log.debug("Content-Length for '{}': {} ({} MiB).", realDownloadUrl, contentLength, contentLength.toLong() / (1024 * 1024))

    new BufferedOutputStream(new FileOutputStream(gradleDownloaderInfo.downloadTargetFileAbsolutePath), 1024 * 1024).withCloseable { BufferedOutputStream fileOutputStream ->
      Long downloadedBytesCount = 0
      streamingHttpClient.exchangeStream(HttpRequest.GET(realDownloadUrl).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE))
                         .map({ HttpResponse<ByteBuffer<?>> byteBufferHttpResponse ->
                           byte[] byteArray = byteBufferHttpResponse.body.orElseThrow({ new NoSuchElementException("No value present") }).toByteArray()
                           downloadedBytesCount += byteArray.length
                           printOutDownloadProgress(realDownloadUrl, downloadedBytesCount, contentLength)

                           return byteArray
                         })
                         .blockingSubscribe(
                             { byte[] byteArray -> fileOutputStream.write(byteArray) } as Consumer,
                             Functions.ON_ERROR_MISSING,
                             { printlnOutNewline() }
                         )
    }

    return new File(gradleDownloaderInfo.downloadTargetFileAbsolutePath)
  }

  protected Tuple2<String, String> calculateRealDownloadUrlAndContentLength(GradleDownloaderInfo gradleDownloaderInfo) {
    HttpResponse<?> headResponse = fetchFirstHeadResponse(gradleDownloaderInfo.fullDownloadUrl)

    String contentLength
    String realDownloadUrl = gradleDownloaderInfo.fullDownloadUrl
    if (HttpStatus.MOVED_PERMANENTLY == headResponse.status) {
      String newLocationUrl = headResponse.header("Location")
      log.debug("HTTP status for '{}': {}({}). New location is '{}'.", gradleDownloaderInfo.downloadSiteUrl, HttpStatus.MOVED_PERMANENTLY.reason, HttpStatus.MOVED_PERMANENTLY.code, newLocationUrl)

      realDownloadUrl = newLocationUrl
      contentLength = fetchRedirectedHeadResponse(realDownloadUrl).header(HttpHeaders.CONTENT_LENGTH) ?: "-1"
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

  protected HttpResponse<?> fetchRedirectedHeadResponse(String url) {
    HttpResponse<?> secondHeadResponse = fetchGeneralHeadResponse(url)

    if (secondHeadResponse.status.code != HttpStatus.OK.code) {
      String message = "HEAD request for '${ url }' returned unexpected HTTP response status: [${ secondHeadResponse.status.reason }(${ secondHeadResponse.status.code })]. " +
                       "Expected HTTP response status is [${ HttpStatus.OK.reason }(${ HttpStatus.OK.code })]. Cannot continue."
      throw new IllegalStateException(message)
    }

    return secondHeadResponse
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

  @SuppressWarnings("Println")
  protected void printOutDownloadProgress(String realDownloadUrl, long downloadedBytesCount, String contentLength) {
    printf("\rDownloading '${ realDownloadUrl }': %d%%", (downloadedBytesCount * 100 / contentLength.toLong()).toLong())
  }

  @SuppressWarnings("Println")
  protected void printlnOutNewline() {
    println ""
  }
}
