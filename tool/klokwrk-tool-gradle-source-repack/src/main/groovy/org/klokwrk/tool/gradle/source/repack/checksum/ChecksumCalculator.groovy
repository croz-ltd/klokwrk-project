package org.klokwrk.tool.gradle.source.repack.checksum

import groovy.transform.CompileStatic

import java.security.MessageDigest

/**
 * Generic utility for calculating checksum of files.
 */
@CompileStatic
class ChecksumCalculator {
  /**
   * Calculates file checksum as hex encoded string.
   * <p/>
   * Provided {@code digestAlgorithm} must be supported by {@link MessageDigest}.
   */
  static String calculateAsHexEncodedString(File file, String digestAlgorithm) {
    return calculateAsBytes(file, digestAlgorithm).encodeHex()
  }

  /**
   * Calculates file checksum as byte array.
   * <p/>
   * Provided {@code digestAlgorithm} must be supported by {@link MessageDigest}.
   */
  static byte[] calculateAsBytes(File file, String digestAlgorithm) {
    MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm)
    Integer bufferSize = 1024 * 1024

    file.withInputStream { InputStream bufferedInputStream ->
      bufferedInputStream.eachByte(bufferSize, { byte[] byteBuffer, Integer bytesRead ->
        messageDigest.update(byteBuffer, 0, bytesRead)
      })
    }

    return messageDigest.digest()
  }
}
