package org.klokwrk.tool.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Encapsulates data needed for repackaging of Gradle sources from Gradle main distribution ZIP file.
 */
@Immutable(includeNames = true)
@CompileStatic
class GradleSourceRepackagerInfo {
  /**
   * Absolute path of Gradle distribution ZIP file from which original sources will be read.
   */
  String gradleDistributionZipFilePath

  /**
   * Gradle version.
   */
  String gradleVersion

  /**
   * Absolute path of an archive into which we will place repacked Gradle sources.
   * <p/>
   * It consists of archive file name prefixed with {@code gradleApiDirPath}.
   */
  String gradleApiSourcesFilePath

  /**
   * Absolute path of directory into which we will place an archive with repacked Gradle sources.
   * <p/>
   * By default it corresponds to the {@code generated-gradle-jars} subdirectory of directory where Gradle caches its distribution:
   * {@code System.getProperty( "user.home" ) + "/.gradle/caches/" + this.gradleVersion + "/generated-gradle-jars"}. If that directory does not exists it falls back to the directory where Gradle
   * distribution is downloaded, which is current working directory by default.
   */
  String gradleApiDirPath

  /**
   * Path to the {@code src} directory inside of Gradle distribution archive (e.g. {@code gradle-6.7.1/src/} ).
   */
  String getGradleDistributionSrcDirPath() {
    return "gradle-${ gradleVersion }/src/"
  }
}
