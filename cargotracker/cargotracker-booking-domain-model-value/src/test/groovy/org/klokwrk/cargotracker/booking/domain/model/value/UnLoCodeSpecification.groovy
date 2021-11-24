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
package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class UnLoCodeSpecification extends Specification {

  void "map constructor should work for correct input params"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: codeParameter)

    then:
    unLoCode.code == codeParameter

    where:
    codeParameter | _
    "HRRJK"       | _
    "HRRJ2"       | _
    "HRRJ9"       | _
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new UnLoCode(code: codeParameter)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    codeParameter | errorMessagePartParam
    null          | "not(blankOrNullString())"
    ""            | "not(blankOrNullString())"
    "   "         | "not(blankOrNullString())"
    "a"           | "hasLength"
    "0"           | "hasLength"
    "hrrjk"       | "matchesPattern"
    "HRR0K"       | "matchesPattern"
    "HRRJ0"       | "matchesPattern"
  }

  void "getCountryCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.countryCode == "HR"
  }

  void "getLocationCode() should return expected value"() {
    when:
    UnLoCode unLoCode = new UnLoCode(code: "HRRJK")

    then:
    unLoCode.locationCode == "RJK"
  }
}
