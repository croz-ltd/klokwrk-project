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
package org.klokwrk.cargotracking.lib.boundary.api.domain.violation

import spock.lang.Specification

class ViolationCodeSpecification extends Specification {
  void "map constructor should work for valid parameters - all available parameters"() {
    when:
    ViolationCode violationCode = new ViolationCode(
        code: "someCode", codeMessage: "someCodeMessage", resolvableMessageKey: "someResolvableMessageKey", resolvableMessageParameters: resolvableMessageParametersParam
    )

    then:
    violationCode
    violationCode.code == "someCode"
    violationCode.codeMessage == "someCodeMessage"
    violationCode.resolvableMessageKey == "someResolvableMessageKey"
    violationCode.resolvableMessageParameters == resolvableMessageParametersParam
    violationCode.isResolvable()

    where:
    resolvableMessageParametersParam | _
    []                               | _
    ["paramOne", "paramTwo"]         | _
  }

  void "map constructor should work for valid parameters - required parameters only"() {
    when:
    ViolationCode violationCode = new ViolationCode(code: "someCode", codeMessage: "someCodeMessage")

    then:
    violationCode
    violationCode.code == "someCode"
    violationCode.codeMessage == "someCodeMessage"
    violationCode.resolvableMessageKey == ViolationCode.RESOLVABLE_MESSAGE_KEY_UNAVAILABLE
    violationCode.resolvableMessageParameters == []
    !violationCode.isResolvable()
  }

  void "map constructor should fail for invalid parameters"() {
    when:
    new ViolationCode(
        code: codeParam, codeMessage: codeMessageParam, resolvableMessageKey: resolvableMessageKeyParam, resolvableMessageParameters: resolvableMessageParametersParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messageParam)

    where:
    codeParam       | codeMessageParam  | resolvableMessageKeyParam  | resolvableMessageParametersParam | messageParam
    null            | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"
    ""              | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"
    " "             | "someCodeMessage" | "someResolvableMessageKey" | []                               | "item: code, expected: not(blankOrNullString())"

    "someCodeParam" | null              | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"
    "someCodeParam" | ""                | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"
    "someCodeParam" | " "               | "someResolvableMessageKey" | []                               | "item: codeMessage, expected: not(blankOrNullString())"

    "someCodeParam" | "someCodeMessage" | null                       | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"
    "someCodeParam" | "someCodeMessage" | ""                         | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"
    "someCodeParam" | "someCodeMessage" | " "                        | []                               | "item: resolvableMessageKey, expected: not(blankOrNullString())"

    "someCodeParam" | "someCodeMessage" | "someResolvableMessageKey" | null                             | "item: resolvableMessageParameters, expected: notNullValue()"
  }

  void "make factory method should work as expected when optional parameters are not provided"() {
    when:
    ViolationCode violationCode = ViolationCode.make("someString", "someCodeMessage")

    then:
    violationCode
    violationCode.code == "someString"
    violationCode.codeMessage == "someCodeMessage"
    violationCode.resolvableMessageKey == ViolationCode.RESOLVABLE_MESSAGE_KEY_UNAVAILABLE
    violationCode.resolvableMessageParameters == []
  }

  void "make factory method should work as expected when optional parameters are provided"() {
    when:
    ViolationCode violationCode = ViolationCode.make("someString", "someCodeMessage", "someResolvableMessageKey")

    then:
    violationCode
    violationCode.code == "someString"
    violationCode.codeMessage == "someCodeMessage"
    violationCode.resolvableMessageKey == "someResolvableMessageKey"
    violationCode.resolvableMessageParameters == []

    and:
    when:
    violationCode = ViolationCode.make("someString", "someCodeMessage", "someResolvableMessageKey", ["paramOne", "paramTwo"])

    then:
    violationCode
    violationCode.code == "someString"
    violationCode.codeMessage == "someCodeMessage"
    violationCode.resolvableMessageKey == "someResolvableMessageKey"
    violationCode.resolvableMessageParameters == ["paramOne", "paramTwo"]
  }

  void "make factory method should fail for invalid value of optional parameter"() {
    when:
    ViolationCode.make("someString", "someCodeMessage", resolvableMessageKeyParam, resolvableMessageParametersParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessageParam)

    where:
    resolvableMessageKeyParam  | resolvableMessageParametersParam | errorMessageParam
    null                       | ["paramOne", "paramTwo"]         | "[item: resolvableMessageKey, expected: not(blankOrNullString()), actual: null]"
    ""                         | ["paramOne", "paramTwo"]         | "[item: resolvableMessageKey, expected: not(blankOrNullString()), actual: ]"
    "  "                       | ["paramOne", "paramTwo"]         | "[item: resolvableMessageKey, expected: not(blankOrNullString()), actual:   ]"
    "someResolvableMessageKey" | null                             | "[item: resolvableMessageParameters, expected: notNullValue(), actual: null]"
  }
}
