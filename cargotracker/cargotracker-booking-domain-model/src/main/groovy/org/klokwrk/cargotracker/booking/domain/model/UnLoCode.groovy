/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.domain.model

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import java.util.regex.Pattern

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.Matchers.not

/**
 * Code conforming to the UN/LOCODE standard.
 * <p/>
 * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm
 */
@KwrkImmutable
@CompileStatic
class UnLoCode implements PostMapConstructorCheckable {

  // Note: be careful to put this static field before UNKNOWN_UN_LO_CODE. Otherwise there will be NPE while constructing UNKNOWN_UN_LO_CODE.
  static final Pattern CODE_PATTERN = Pattern.compile(CommonConstants.REGEX_UN_LO_CODE)

  static final UnLoCode UNKNOWN_UN_LO_CODE = new UnLoCode(code: "NAUNK")

  /**
   * Code for the UnLoCode location.
   * <p/>
   * The two first digits in indicates the country in which the place is located. The values used concur with the  ISO 3166 alpha-2 Country Code.
   * <p/>
   * In the next part of the code you can find a 3-character code for the location. The 3-character code element for the location will normally comprise three letters. However, where all permutations
   * available for a country have been exhausted, the numerals 2-9 may also be used.
   */
  String code

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(code, not(blankOrNullString()))
    requireMatch(code, matchesPattern(CODE_PATTERN))
  }

  /**
   * Fetches ISO 3166 alpha-2 Country Code.
   */
  String getCountryCode() {
    return code[0..1]
  }

  /**
   * The part of full UN/LOCODE which refers to the location itself.
   */
  String getLocationCode() {
    return code[2..4]
  }
}
