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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import java.util.regex.Pattern

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.hasLength
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.Matchers.not

/**
 * Represents an 8-character function classifier code for the UN/LOCODE location.
 * <p/>
 * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm - Section: 1.6 Column "Function"
 */
@SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
@KwrkImmutable
@CompileStatic
class UnLoCodeFunction implements PostMapConstructorCheckable {
  static final Pattern CODE_PATTERN = Pattern.compile(/^(?=.*[0-7B].*)[01-][2-][3-][4-][5-][6-][7-][B-]$/)

  static final UnLoCodeFunction UNKNOWN_UN_LO_CODE_FUNCTION = new UnLoCodeFunction(functionEncoded: "0-------")

  String functionEncoded

  static UnLoCodeFunction makeWithPortClassifier() {
    return new UnLoCodeFunction(functionEncoded: "1-------")
  }

  static UnLoCodeFunction copyWithPortClassifier(UnLoCodeFunction unLoCodeFunctionOriginal) {
    if (unLoCodeFunctionOriginal.functionEncoded[0] == "1") {
      return unLoCodeFunctionOriginal
    }

    StringBuilder builder = new StringBuilder(unLoCodeFunctionOriginal.functionEncoded)
    builder.setCharAt(0, '1' as char)

    return new UnLoCodeFunction(functionEncoded: builder.toString())
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(functionEncoded, not(blankOrNullString()))
    requireMatch(functionEncoded, hasLength(8))
    requireMatch(functionEncoded, matchesPattern(CODE_PATTERN))
  }

  Boolean isSpecified() {
    return functionEncoded[0] != "0"
  }

  Boolean isPort() {
    return functionEncoded[0] == "1"
  }

  Boolean isRailTerminal() {
    return functionEncoded[1] == "2"
  }

  Boolean isRoadTerminal() {
    return functionEncoded[2] == "3"
  }

  Boolean isAirport() {
    return functionEncoded[3] == "4"
  }

  Boolean isPostalExchangeOffice() {
    return functionEncoded[4] == "5"
  }

  Boolean isBorderCrossing() {
    return functionEncoded[7] == "B"
  }
}
