package org.klokwrk.tool.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Repackages Gradle sources into a sources archive understood by IDEA.
 */
// TODO dmurat: revise Indentation warning suppression after analysis and potential bug report to CodeNarc.
@SuppressWarnings(["JavaIoPackageAccess", "Indentation"])
@CompileStatic
class GradleSourceRepackager {
  private static final Logger log = LoggerFactory.getLogger(GradleSourceRepackager)

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
    repackageZipFile(repackagerInfo, countOfTargetZipEntries)
  }

  private static Long calculateCountOfTargetZipEntries(GradleSourceRepackagerInfo repackagerInfo) {
    Long countOfTargetZipEntries
    try (ZipFile zipFile = new ZipFile(repackagerInfo.gradleDistributionZipFilePath)) {
      countOfTargetZipEntries = zipFile
          .stream()
          .filter((ZipEntry zipEntry) -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath))
          .map((ZipEntry zipEntry) -> calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, zipEntry))
          .distinct() // skipping duplicate target entries (e.g. package-info.java)
          .count()
    }

    return countOfTargetZipEntries
  }

  private static String calculateTargetZipEntryName(String gradleDistributionSrcDirPath, ZipEntry originalZipEntry) {
    String sourceZipEntryFullName = originalZipEntry.name
    String sourceZipEntryWithoutPrefix = sourceZipEntryFullName - gradleDistributionSrcDirPath

    String targetZipEntryName = sourceZipEntryWithoutPrefix[(sourceZipEntryWithoutPrefix.indexOf("/") + 1)..-1]
    return targetZipEntryName
  }

  private static void repackageZipFile(GradleSourceRepackagerInfo repackagerInfo, Long countOfTargetZipEntries) {
    try (ZipOutputStream targetZipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(repackagerInfo.gradleApiSourcesFilePath)))) {
      try (ZipFile originalZipFile = new ZipFile(repackagerInfo.gradleDistributionZipFilePath)) {
        AtomicLong zipEntriesProcessedCount = new AtomicLong(0)
        originalZipFile
            .stream()
            .filter((ZipEntry zipEntry) -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath))
            .forEach((ZipEntry originalZipEntry) -> {
              String targetZipEntryName = calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, originalZipEntry)
              String skippedMessage = repackageZipEntry(originalZipFile, originalZipEntry, targetZipOutputStream, targetZipEntryName)

              if (!skippedMessage) {
                zipEntriesProcessedCount.accumulateAndGet(1, Long::sum)
              }

              Integer percentage = zipEntriesProcessedCount.get() * 100 / countOfTargetZipEntries as Integer
              Boolean isLastEntry = countOfTargetZipEntries == zipEntriesProcessedCount.get()
              String newLineIfNecessary = (isLastEntry || (skippedMessage && log.debugEnabled) || log.traceEnabled) ? "\n" : ""
              printRepackagingProgressOnConsole(repackagerInfo.gradleApiSourcesFilePath, percentage, newLineIfNecessary)

              if (skippedMessage) {
                log.debug(skippedMessage)
              }
              else {
                log.trace("Repacked Gradle source file: {} -> {}", originalZipEntry.name, targetZipEntryName)
              }
            })
      }
    }
  }

  private static String repackageZipEntry(ZipFile originalZipFile, ZipEntry originalZipEntry, ZipOutputStream targetZipOutputStream, String targetZipEntryName) {
    String skippedMessage = null
    try (InputStream inputStream = originalZipFile.getInputStream(originalZipEntry)) {
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

  @SuppressWarnings("Println")
  private static void printRepackagingProgressOnConsole(String gradleApiSourcesFilePath, Integer percentage, String newLineIfNecessary) {
    print "\rRepackaging into ${ gradleApiSourcesFilePath }: ${ percentage }%${ newLineIfNecessary }"
  }
}
