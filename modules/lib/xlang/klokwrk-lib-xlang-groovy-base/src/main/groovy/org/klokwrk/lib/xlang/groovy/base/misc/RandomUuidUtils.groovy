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
package org.klokwrk.lib.xlang.groovy.base.misc

import groovy.transform.CompileStatic

/**
 * Various helper methods for working with random UUID (version 4, variant 2).
 */
@CompileStatic
class RandomUuidUtils {
  static final Integer VALID_UUID_VERSION = 4
  static final Integer VALID_UUID_VARIANT = 2

  /**
   * Checks if provided string represents random UUID (uuid version 4 and uuid variant 2, i.e. {@code 00000000-0000-4000-8000-000000000000}).
   */
  @SuppressWarnings("CodeNarc.CatchException")
  static boolean checkIfRandomUuidString(String uuidStringToCheck) {
    if (!uuidStringToCheck) {
      return false
    }

    UUID uuid

    try {
      uuid = UUID.fromString(uuidStringToCheck)
    }
    catch (Exception ignore) {
      return false
    }

    return checkIfRandomUuid(uuid)
  }

  /**
   * Checks if provided UUID is of version 4 and variant 2, i.e. {@code 00000000-0000-4000-8000-000000000000}).
   */
  static boolean checkIfRandomUuid(UUID uuidToCheck) {
    if (!uuidToCheck) {
      return false
    }

    boolean isVersionValid = uuidToCheck.version() == VALID_UUID_VERSION
    boolean isVariantValid = uuidToCheck.variant() == VALID_UUID_VARIANT
    return isVersionValid && isVariantValid
  }
}
