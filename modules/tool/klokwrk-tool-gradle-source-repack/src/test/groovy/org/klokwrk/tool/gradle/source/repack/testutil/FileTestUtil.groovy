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

  /**
   * Recursively deletes specified directory, or just delete a file when not directory.
   */
  static void delete(File fileToDelete) {
    if (fileToDelete.directory) {
      File[] childFileList = fileToDelete.listFiles()
      if (childFileList?.size() > 0) {
        childFileList.each { File file ->
          delete(file)
        }
      }

      fileToDelete.delete()
    }
    else {
      fileToDelete.delete()
    }
  }
}
