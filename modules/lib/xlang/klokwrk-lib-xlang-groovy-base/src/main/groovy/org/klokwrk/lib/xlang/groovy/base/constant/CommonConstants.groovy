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
package org.klokwrk.lib.xlang.groovy.base.constant

import groovy.transform.CompileStatic

/**
 * Constants to be used from any module of the project.
 */
@CompileStatic
class CommonConstants {
  /**
   * Used when string value represents that something is missing.
   */
  public static final String NOT_AVAILABLE = "n/a"

  /**
   * Regex for verifying if string complies to UUID format.
   */
  public static final String REGEX_UUID_FORMAT = /^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$/

  /**
   * Regex for verifying if string complies to UN/LOCODE format.
   * <p/>
   * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm - Section "1.2 Column LOCODE".
   * <p/>
   * The two first digits indicates the country in which the place is located. The values used concur with the  ISO 3166 alpha-2 Country Code. In cases where no ISO 3166 country code element is
   * available, e.g. installations in international waters or international cooperation zones, the code element "XZ" will be used.
   * <p/>
   * Next part contains a 3-character code for the location. The 3-character code element for the location will normally comprise three letters. However, where all permutations available for a
   * country have been exhausted, the numerals 2-9 may also be used.
   */
  public static final String REGEX_UN_LO_CODE = /^[A-Z]{4}[A-Z2-9]$/
}
