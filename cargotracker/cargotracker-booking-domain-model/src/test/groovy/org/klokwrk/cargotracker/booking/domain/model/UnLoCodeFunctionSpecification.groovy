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
    thrown(AssertionError)

    where:
    functionParameter | _
    null              | _
    ""                | _
    "   "             | _
    "1"               | _
    "--------"        | _
    "2-------"        | _
    "-1------"        | _
    "-0------"        | _
    "11------"        | _
    "1------"         | _
    "1--------"       | _
    "1------A"        | _
    "1------8"        | _
    "-------b"        | _
  }

  void "createWithPortClassifier() should work as expected"() {
    when:
    UnLoCodeFunction unLoCodeFunction = UnLoCodeFunction.createWithPortClassifier()

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
