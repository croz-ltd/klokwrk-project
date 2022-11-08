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

import spock.lang.Specification

class UnLoCodeFunctionSpecification extends Specification {
  void "map constructor should work for correct input params"() {
    when:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    then:
    unLoCodeFunction.functionEncoded == functionParameter

    where:
    functionParameter | _
    "0-------"        | _
    "1-------"        | _
    "1234----"        | _
    "-------B"        | _
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new UnLoCodeFunction(functionEncoded: functionParameter)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    functionParameter | errorMessagePartParam
    null              | "not(blankOrNullString())"
    ""                | "not(blankOrNullString())"
    "   "             | "not(blankOrNullString())"
    "1"               | "hasLength"
    "--------"        | "matchesPattern"
    "2-------"        | "matchesPattern"
    "-1------"        | "matchesPattern"
    "-0------"        | "matchesPattern"
    "11------"        | "matchesPattern"
    "1------"         | "hasLength"
    "1--------"       | "hasLength"
    "1------A"        | "matchesPattern"
    "1------8"        | "matchesPattern"
    "-------b"        | "matchesPattern"
  }

  void "makeWithPortClassifier() should work as expected"() {
    when:
    UnLoCodeFunction unLoCodeFunction = UnLoCodeFunction.makeWithPortClassifier()

    then:
    unLoCodeFunction.functionEncoded == "1-------"
  }

  void "copyWithPortClassifier() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunctionOriginal = new UnLoCodeFunction(functionEncoded: "-234---B")

    when:
    UnLoCodeFunction unLoCodeFunctionCopy = UnLoCodeFunction.copyWithPortClassifier(unLoCodeFunctionOriginal)

    then:
    unLoCodeFunctionOriginal !== unLoCodeFunctionCopy
    unLoCodeFunctionCopy.functionEncoded == "1234---B"
  }

  void "copyWithPortClassifier() should return same instance when it already is a port"() {
    given:
    UnLoCodeFunction unLoCodeFunctionOriginal = new UnLoCodeFunction(functionEncoded: "1234---B")

    when:
    UnLoCodeFunction unLoCodeFunctionCopy = UnLoCodeFunction.copyWithPortClassifier(unLoCodeFunctionOriginal)

    then:
    unLoCodeFunctionOriginal === unLoCodeFunctionCopy
  }

  void "isSpecified() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isSpecified() == result
    unLoCodeFunction.specified == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | true
    "-2------"        | true
    "-23-----"        | true
    "-------B"        | true
  }

  void "isPort() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isPort() == result
    unLoCodeFunction.port == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | true
    "-2------"        | false
    "-23-----"        | false
    "-------B"        | false
  }

  void "isRailTerminal() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isRailTerminal() == result
    unLoCodeFunction.railTerminal == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | true
    "-23-----"        | true
    "-------B"        | false
  }

  void "isRoadTerminal() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isRoadTerminal() == result
    unLoCodeFunction.roadTerminal == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "-23-----"        | true
    "--3-----"        | true
    "-------B"        | false
  }

  void "isAirport() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isAirport() == result
    unLoCodeFunction.airport == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "-23-----"        | false
    "-234----"        | true
    "---4----"        | true
    "-------B"        | false
  }

  void "isPostalExchangeOffice() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isPostalExchangeOffice() == result
    unLoCodeFunction.postalExchangeOffice == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "--3-----"        | false
    "---4----"        | false
    "----5---"        | true
    "-------B"        | false
  }

  void "isBorderCrossing() should work as expected"() {
    given:
    UnLoCodeFunction unLoCodeFunction = new UnLoCodeFunction(functionEncoded: functionParameter)

    expect:
    unLoCodeFunction.isBorderCrossing() == result

    where:
    functionParameter | result
    "0-------"        | false
    "1-------"        | false
    "-2------"        | false
    "--3-----"        | false
    "---4----"        | false
    "----5---"        | false
    "-------B"        | true
    "1------B"        | true
  }
}
