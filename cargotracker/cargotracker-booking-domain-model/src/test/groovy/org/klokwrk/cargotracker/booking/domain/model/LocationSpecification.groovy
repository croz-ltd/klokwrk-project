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
    new Location(
        code: new UnLoCode(code: codeParameter), name: new InternationalizedName(name: nameParameter), countryName: new InternationalizedName(name: countryNameParameter),
        unLoCodeFunction: new UnLoCodeFunction(functionParameter)
    )

    then:
    thrown(AssertionError)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter
    null          | "someName"    | "someCountry"        | "0------"
    "HRRJK"       | null          | "someCountry"        | "0------"
    "HRRJK"       | "someName"    | null                 | "0------"
    "HRRJK"       | "someName"    | "someCountry"        | null
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
    thrown(AssertionError)

    where:
    codeParameter | nameParameter | countryNameParameter | functionParameter
    null          | "someName"    | "someCountry"        | "0------"
    "HRRJK"       | null          | "someCountry"        | "0------"
    "HRRJK"       | "someName"    | null                 | "0------"
    "HRRJK"       | "someName"    | "someCountry"        | null
  }

  static Map<String, Location> locationSampleMap = [
      "HRDKO": Location.create("HRDKO", "Đakovo", "Hrvatska", "--3-----"),
      "HRKRK": Location.create("HRKRK", "Krk", "Hrvatska", "1-3-----"),
      "HRMVN": Location.create("HRMVN", "Motovun", "Hrvatska", "--3-----"),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----"),
      "HRVZN": Location.create("HRVZN", "Varaždin", "Hrvatska", "-23-----"),
      "HRZAD": Location.create("HRZAD", "Zadar", "Hrvatska", "1234----"),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Hrvatska", "-2345---")
  ]

  void "destinationLocation.canAcceptCargoFrom() should work as expected"() {
    when:
    Location originLocation = originLocationInstance
    Location destinationLocation = destinationLocationInstance

    then:
    destinationLocation.canAcceptCargoFrom(originLocation) == destinationCanAccept

    where:
    originLocationInstance     | destinationLocationInstance | destinationCanAccept | originDescription              | destinationDescription
    locationSampleMap["HRRJK"] | locationSampleMap["HRRJK"]  | false                | "any"                          | "same as origin"
    null                       | locationSampleMap["HRRJK"]  | false                | "null"                         | "any"
    locationSampleMap["HRZAD"] | locationSampleMap["HRRJK"]  | true                 | "port & rail terminal"         | "port & rail terminal"
    locationSampleMap["HRZAD"] | locationSampleMap["HRKRK"]  | true                 | "port & rail terminal"         | "port"
    locationSampleMap["HRKRK"] | locationSampleMap["HRZAD"]  | true                 | "port"                         | "port & rail terminal"
    locationSampleMap["HRZAG"] | locationSampleMap["HRZAD"]  | true                 | "rail terminal"                | "port & rail terminal"
    locationSampleMap["HRZAG"] | locationSampleMap["HRVZN"]  | true                 | "rail terminal"                | "rail terminal"
    locationSampleMap["HRZAG"] | locationSampleMap["HRKRK"]  | false                | "rail terminal"                | "port"
    locationSampleMap["HRKRK"] | locationSampleMap["HRZAG"]  | false                | "port"                         | "rail terminal"
    locationSampleMap["HRDKO"] | locationSampleMap["HRZAG"]  | false                | "not port & not rail terminal" | "rail terminal"
    locationSampleMap["HRZAG"] | locationSampleMap["HRDKO"]  | false                | "rail terminal"                | "not port & not rail terminal"
    locationSampleMap["HRMVN"] | locationSampleMap["HRDKO"]  | false                | "not port & not rail terminal" | "not port & not rail terminal"
  }
}
