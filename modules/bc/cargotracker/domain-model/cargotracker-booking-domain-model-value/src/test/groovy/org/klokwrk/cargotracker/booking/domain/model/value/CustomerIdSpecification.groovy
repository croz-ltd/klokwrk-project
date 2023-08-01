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
package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class CustomerIdSpecification extends Specification {
  void "map constructor should work for valid arguments"() {
    expect:
    new CustomerId(identifier: identifierParam)

    where:
    identifierParam                        | _
    UUID.randomUUID()                      | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000001" | _
    "11111111-1111-4111-A111-111111111111" | _
  }

  void "map constructor should fail for invalid arguments"() {
    when:
    new CustomerId(identifier: identifierParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    identifierParam                        | errorMessagePartParam
    null                                   | "not(blankOrNullString())"
    ""                                     | "not(blankOrNullString())"
    "   "                                  | "not(blankOrNullString())"

    "1"                                    | "checkIfRandomUuidString(identifier)"
    "Z"                                    | "checkIfRandomUuidString(identifier)"
    " ${ UUID.randomUUID() }"              | "checkIfRandomUuidString(identifier)"
    "${ UUID.randomUUID() } "              | "checkIfRandomUuidString(identifier)"
    " ${ UUID.randomUUID() } "             | "checkIfRandomUuidString(identifier)"

    "00000000-0000-4000-0000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-1000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-7000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-C000-000000000000" | "checkIfRandomUuidString(identifier)"
  }

  void "make() should produce valid CargoId for valid parameter"() {
    when:
    CustomerId.make(uuidStringParam)

    then:
    noExceptionThrown()

    where:
    uuidStringParam                        | _
    UUID.randomUUID().toString()           | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000001" | _
    "11111111-1111-4111-A111-111111111111" | _
  }

  void "make() should fail for invalid parameter"() {
    when:
    CustomerId.make(uuidStringParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    uuidStringParam                        | errorMessagePartParam
    null                                   | "not(blankOrNullString())"
    ""                                     | "not(blankOrNullString())"
    "   "                                  | "not(blankOrNullString())"

    "1"                                    | "checkIfRandomUuidString(identifier)"
    "Z"                                    | "checkIfRandomUuidString(identifier)"
    " ${ UUID.randomUUID() }"              | "checkIfRandomUuidString(identifier)"
    "${ UUID.randomUUID() } "              | "checkIfRandomUuidString(identifier)"
    " ${ UUID.randomUUID() } "             | "checkIfRandomUuidString(identifier)"

    "00000000-0000-4000-0000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-1000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-7000-000000000000" | "checkIfRandomUuidString(identifier)"
    "00000000-0000-4000-C000-000000000000" | "checkIfRandomUuidString(identifier)"
  }
}
