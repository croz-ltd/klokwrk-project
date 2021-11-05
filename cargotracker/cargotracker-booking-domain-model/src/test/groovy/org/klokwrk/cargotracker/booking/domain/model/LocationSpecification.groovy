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

class LocationSpecification extends Specification {

  void "map constructor should work for correct input params"() {
    when:
    Location location = new Location(
        unLoCode: new UnLoCode(code: "HRRJK"), name: new InternationalizedName(name: "Rijeka"), countryName: new InternationalizedName(name: "Hrvatska"),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: "1234----")
    )

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Hrvatska")
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new Location(unLoCode: unLoCodeParam, name: nameParam, countryName: countryNameParam, unLoCodeFunction: unLoCodeFunctionParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    unLoCodeParam               | nameParam                                   | countryNameParam                               | unLoCodeFunctionParam                             | messagePartParam
    null                        | new InternationalizedName(name: "someName") | new InternationalizedName(name: "someCountry") | new UnLoCodeFunction(functionEncoded: "0-------") | "notNullValue"
    new UnLoCode(code: "HRRJK") | null                                        | new InternationalizedName(name: "someCountry") | new UnLoCodeFunction(functionEncoded: "0-------") | "notNullValue"
    new UnLoCode(code: "HRRJK") | new InternationalizedName(name: "someName") | null                                           | new UnLoCodeFunction(functionEncoded: "0-------") | "notNullValue"
    new UnLoCode(code: "HRRJK") | new InternationalizedName(name: "someName") | new InternationalizedName(name: "someCountry") | null                                              | "notNullValue"
  }

  void "map constructor should fail for invalid input params for construction of contained properties"() {
    when:
    new Location(
        unLoCode: new UnLoCode(code: codeParameter), name: new InternationalizedName(name: nameParameter), countryName: new InternationalizedName(name: countryNameParameter),
        unLoCodeFunction: new UnLoCodeFunction(functionEncoded: functionParameter)
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter | errorMessagePartParam
    null          | "someName"    | "someCountry"        | "0-------"        | "item: code, expected: not(blankOrNullString())"
    "HRRJK"       | null          | "someCountry"        | "0-------"        | "item: name, expected: not(blankOrNullString())"
    "HRRJK"       | "someName"    | null                 | "0-------"        | "item: name, expected: not(blankOrNullString())"
    "HRRJK"       | "someName"    | "someCountry"        | null              | "item: functionEncoded, expected: not(blankOrNullString())"
  }

  void "create() factory method should work for correct input params"() {
    when:
    Location location = Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----")

    then:
    location.unLoCode == new UnLoCode(code: "HRRJK")
    location.name == new InternationalizedName(name: "Rijeka")
    location.countryName == new InternationalizedName(name: "Hrvatska")
  }

  void "create() factory method should fail for invalid input params"() {
    when:
    Location.create(codeParameter, nameParameter, countryNameParameter, functionParameter)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter | errorMessagePartParam
    null          | "someName"    | "someCountry"        | "0------"         | "not(blankOrNullString())"
    "HRRJK"       | null          | "someCountry"        | "0------"         | "not(blankOrNullString())"
    "HRRJK"       | "someName"    | null                 | "0------"         | "not(blankOrNullString())"
    "HRRJK"       | "someName"    | "someCountry"        | null              | "not(blankOrNullString())"
  }
}
