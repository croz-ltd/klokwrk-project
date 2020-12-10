package org.klokwrk.tool.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
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
    Long countOfTargetZipEntries = null
    new ZipFile(repackagerInfo.gradleDistributionZipFilePath).withCloseable { ZipFile zipFile ->
      countOfTargetZipEntries = zipFile
          .stream()
          .filter({ ZipEntry zipEntry -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath) } as Predicate)
          .map({ ZipEntry zipEntry -> calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, zipEntry) } as Function)
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
    new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(repackagerInfo.gradleApiSourcesFilePath))).withCloseable { ZipOutputStream targetZipOutputStream ->
      new ZipFile(repackagerInfo.gradleDistributionZipFilePath).withCloseable { ZipFile originalZipFile ->
        Long zipEntriesProcessedCount = new Long(0)
        originalZipFile
            .stream()
            .filter({ ZipEntry zipEntry -> !zipEntry.isDirectory() && zipEntry.name.startsWith(repackagerInfo.gradleDistributionSrcDirPath) } as Predicate)
            .forEach({ ZipEntry originalZipEntry ->
              String targetZipEntryName = calculateTargetZipEntryName(repackagerInfo.gradleDistributionSrcDirPath, originalZipEntry)
              String skippedMessage = repackageZipEntry(originalZipFile, originalZipEntry, targetZipOutputStream, targetZipEntryName)

              if (!skippedMessage) {
                zipEntriesProcessedCount++
              }

              Integer percentage = zipEntriesProcessedCount * 100 / countOfTargetZipEntries as Integer
              Boolean isLastEntry = countOfTargetZipEntries == zipEntriesProcessedCount
              String newLineIfNecessary = (isLastEntry || (skippedMessage && log.debugEnabled) || log.traceEnabled) ? "\n" : ""
              printRepackagingProgressOnConsole(repackagerInfo.gradleApiSourcesFilePath, percentage, newLineIfNecessary)

              if (skippedMessage) {
                log.debug(skippedMessage)
              }
              else {
                log.trace("Repacked Gradle source file: {} -> {}", originalZipEntry.name, targetZipEntryName)
              }

              //noinspection GroovyUnnecessaryReturn
              return // Note: this return is here just to make JaCoCo report more reliable.
            } as Consumer)
      }
    }
  }

  private static String repackageZipEntry(ZipFile originalZipFile, ZipEntry originalZipEntry, ZipOutputStream targetZipOutputStream, String targetZipEntryName) {
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

  @SuppressWarnings("Println")
  private static void printRepackagingProgressOnConsole(String gradleApiSourcesFilePath, Integer percentage, String newLineIfNecessary) {
    print "\rRepackaging into ${ gradleApiSourcesFilePath }: ${ percentage }%${ newLineIfNecessary }"
  }
}
