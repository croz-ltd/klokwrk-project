/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.misc

import groovy.transform.CompileStatic

@CompileStatic
class UUIDUtils {

  /**
   * Checks if provided string represents random UUID (uuid version 4 and uuid variant 2, i.e. {@code 00000000-0000-4000-8000-000000000000}).
   */
  @SuppressWarnings("CodeNarc.CatchException")
  static Boolean checkIfRandomUuid(String uuidStringToCheck) {
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

    Boolean isVersionValid = uuid.version() == 4
    Boolean isVariantValid = uuid.variant() == 2
    return isVersionValid && isVariantValid
  }
}
