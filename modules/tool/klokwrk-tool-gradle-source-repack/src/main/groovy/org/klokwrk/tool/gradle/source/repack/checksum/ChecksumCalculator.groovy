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
