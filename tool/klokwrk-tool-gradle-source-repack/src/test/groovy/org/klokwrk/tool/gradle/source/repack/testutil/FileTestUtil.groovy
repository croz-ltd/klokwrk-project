package org.klokwrk.tool.gradle.source.repack.testutil

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Paths

@CompileStatic
class FileTestUtil {
  static Map<String, File> prepareDirectoriesAndFiles() {
    String downloadDirPath = "${ System.getProperty("user.dir") }/build/_testrun/${ UUID.randomUUID() }/"
    Files.createDirectories(Paths.get(downloadDirPath))
    File downloadDir = new File(downloadDirPath)

    File downloadedGradleDistributionFile = new File("${ downloadDir }/gradle-6.7.1-all.zip")
    downloadedGradleDistributionFile.delete()
    File downloadedGradleDistributionSha256File = new File("${ downloadDir }/gradle-6.7.1-all.zip.sha256")
    downloadedGradleDistributionSha256File.delete()

    File repackDir = new File(downloadDirPath)
    File repackedSourceArchiveFile = new File("${ repackDir }/gradle-api-6.7.1-sources.jar")
    repackedSourceArchiveFile.delete()

    Map testDirectoriesAndFiles = [
        downloadDir: downloadDir,
        downloadedGradleDistributionFile: downloadedGradleDistributionFile,
        downloadedGradleDistributionSha256File: downloadedGradleDistributionSha256File,
        repackDir: repackDir,
        repackedSourceArchiveFile: repackedSourceArchiveFile
    ]

    return testDirectoriesAndFiles
  }

  static void cleanupDirectoriesAndFiles(Map<String, File> testDirectoriesAndFiles) {
    testDirectoriesAndFiles.downloadedGradleDistributionFile.delete()
    testDirectoriesAndFiles.downloadedGradleDistributionSha256File.delete()
    testDirectoriesAndFiles.repackedSourceArchiveFile.delete()
    testDirectoriesAndFiles.repackDir.delete()
    testDirectoriesAndFiles.downloadDir.delete()
  }
}
