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
package org.klokwrk.tool.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Repackages Gradle sources into a sources archive understood by IDEA.
 */
@SuppressWarnings("CodeNarc.JavaIoPackageAccess")
@CompileStatic
class GradleSourceRepackager {
  private static final Logger log = LoggerFactory.getLogger(GradleSourceRepackager)

  @SuppressWarnings("CodeNarc.Println")
  static void repackGradleSource(GradleSourceRepackagerInfo repackagerInfo) {
    log.debug("Starting repackaging with following gradleSourceRepackagerInfo: {}", repackagerInfo)
    log.debug("Repackaging source and target: '{}' ==> '{}'.", repackagerInfo.gradleDistributionZipFilePath, repackagerInfo.gradleApiSourcesFilePath)

    File gradleDistributionZipFile = new File(repackagerInfo.gradleDistributionZipFilePath)
    if (!gradleDistributionZipFile.exists()) {
      throw new IllegalStateException("Source Gradle distribution does not exist: [${ gradleDistributionZipFile.absolutePath }]")
    }

    File gradleApiDir = new File(repackagerInfo.gradleApiDirPath)
    if (!gradleApiDir.exists()) {
      throw new IllegalStateException("Target directory for repacking does not exist: [${ gradleApiDir.absolutePath }]")
    }

    log.info("Repackaging Gradle sources: {} ===> {}", repackagerInfo.gradleDistributionZipFilePath, repackagerInfo.gradleApiSourcesFilePath)

    Long countOfTargetZipEntries = calculateCountOfTargetZipEntries(repackagerInfo)
    List<String> skippedMessageList = repackageZipFile(repackagerInfo, countOfTargetZipEntries)
    if (skippedMessageList && log.isDebugEnabled()) {
      println ""
      log.debug("During repackaging the following entries were skipped:")
      skippedMessageList.each({ String skippedMessage -> log.debug(skippedMessage) })
    }
  }

  protected static Long calculateCountOfTargetZipEntries(GradleSourceRepackagerInfo repackagerInfo) {
    Long countOfTargetZipEntries = null
    new ZipFile(repackagerInfo.gradleDistributionZipFilePath).withCloseable { ZipFile zipFile ->
      countOfTargetZipEntries = zipFile
          .stream()
          .filter({ ZipEntry zipEntry -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath) })
          .map({ ZipEntry zipEntry -> calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, zipEntry) })
          .distinct() // skipping duplicate target entries (e.g. package-info.java)
          .count()
    }

    return countOfTargetZipEntries
  }

  protected static String calculateTargetZipEntryName(String gradleDistributionSrcDirPath, ZipEntry originalZipEntry) {
    String sourceZipEntryFullName = originalZipEntry.name
    String sourceZipEntryWithoutPrefix = sourceZipEntryFullName - gradleDistributionSrcDirPath

    String targetZipEntryName = sourceZipEntryWithoutPrefix[(sourceZipEntryWithoutPrefix.indexOf("/") + 1)..-1]
    return targetZipEntryName
  }

  @SuppressWarnings(["CodeNarc.Indentation", "CodeNarc.Println"])
  protected static List<String> repackageZipFile(GradleSourceRepackagerInfo repackagerInfo, Long countOfTargetZipEntries) {
    List<String> skippedMessageList = []
    new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(repackagerInfo.gradleApiSourcesFilePath))).withCloseable { ZipOutputStream targetZipOutputStream ->
      new ZipFile(repackagerInfo.gradleDistributionZipFilePath).withCloseable { ZipFile originalZipFile ->
        Long zipEntriesProcessedCount = Long.valueOf(0)
        originalZipFile
            .stream()
            .filter({ ZipEntry zipEntry -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath) })
            .forEach({ ZipEntry originalZipEntry ->
              String targetZipEntryName = calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, originalZipEntry)
              String skippedMessage = repackageZipEntry(originalZipFile, originalZipEntry, targetZipOutputStream, targetZipEntryName)

              if (skippedMessage) {
                skippedMessageList.add(skippedMessage)
              }
              else {
                zipEntriesProcessedCount++
                if (log.isTraceEnabled()) {
                  println ""
                  log.trace("Repacked Gradle source file: {} -> {}", originalZipEntry.name, targetZipEntryName)
                }
              }

              Integer percentage = (zipEntriesProcessedCount * 100 / countOfTargetZipEntries).toInteger()
              printRepackagingProgressOnConsole(repackagerInfo.gradleApiSourcesFilePath, percentage)

              //noinspection GroovyUnnecessaryReturn
              return // Note: this return is here just to make JaCoCo report more reliable.
            })
      }
    }

    return skippedMessageList
  }

  protected static String repackageZipEntry(ZipFile originalZipFile, ZipEntry originalZipEntry, ZipOutputStream targetZipOutputStream, String targetZipEntryName) {
    String skippedMessage = null
    originalZipFile.getInputStream(originalZipEntry).withCloseable { InputStream inputStream ->
      try {
        targetZipOutputStream.putNextEntry(new ZipEntry(targetZipEntryName))

        byte[] bytes = new byte[1024]
        int length
        while ((length = inputStream.read(bytes)) >= 0) {
          targetZipOutputStream.write(bytes, 0, length)
        }
      }
      catch (ZipException zipException) {
        if (zipException.message.contains("duplicate entry")) {
          skippedMessage = "Skipping ${ zipException.message }."
        }
        else {
          throw zipException
        }
      }
    }

    return skippedMessage
  }

  @SuppressWarnings("CodeNarc.Println")
  private static void printRepackagingProgressOnConsole(String gradleApiSourcesFilePath, Integer percentage) {
    print "\rRepackaging into ${ gradleApiSourcesFilePath }: ${ percentage }%"
  }
}
