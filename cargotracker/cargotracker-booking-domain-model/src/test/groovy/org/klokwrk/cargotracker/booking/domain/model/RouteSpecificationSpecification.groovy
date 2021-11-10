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

class RouteSpecificationSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "HRDKO": Location.create("HRDKO", "Đakovo", "Hrvatska", "--3-----"),
      "HRKRK": Location.create("HRKRK", "Krk", "Hrvatska", "1-3-----"),
      "HRMVN": Location.create("HRMVN", "Motovun", "Hrvatska", "--3-----"),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----"),
      "HRVZN": Location.create("HRVZN", "Varaždin", "Hrvatska", "-23-----"),
      "HRZAD": Location.create("HRZAD", "Zadar", "Hrvatska", "1234----"),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Hrvatska", "-2345---")
  ]

  void "map constructor should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = new RouteSpecification(originLocation: locationSampleMap["HRRJK"], destinationLocation: locationSampleMap["HRZAG"])

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "HRZAG"
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new RouteSpecification(originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    originLocationParam        | destinationLocationParam   | messagePartParam
    null                       | locationSampleMap["HRRJK"] | "notNullValue"
    locationSampleMap["HRRJK"] | null                       | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["HRRJK"] | "not(sameInstance(destinationLocation))"
  }

  void "create() factory method should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = RouteSpecification.create(locationSampleMap["HRRJK"], locationSampleMap["HRZAG"])

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "HRZAG"
  }

  void "create() factory method should fail for invalid input params"() {
    when:
    RouteSpecification.create(originLocationParam, destinationLocationParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(errorMessagePartParam)

    where:
    originLocationParam        | destinationLocationParam   | errorMessagePartParam
    null                       | locationSampleMap["HRRJK"] | "notNullValue"
    locationSampleMap["HRRJK"] | null                       | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["HRRJK"] | "not(sameInstance(destinationLocation))"
  }

  void "canDestinationAcceptCargoFromOrigin() should work as expected"() {
    when:
    RouteSpecification routeSpecification = new RouteSpecification(originLocation: originLocationInstance, destinationLocation: destinationLocationInstance)

    then:
    routeSpecification.canDestinationAcceptCargoFromOrigin() == destinationCanAccept

    where:
    originLocationInstance                                     | destinationLocationInstance | destinationCanAccept | originDescription              | destinationDescription
    Location.create("HRRJK", "Rijeka", "Hrvatska", "1234----") | locationSampleMap["HRRJK"]  | false                | "any"                          | "same as origin"
    locationSampleMap["HRZAD"]                                 | locationSampleMap["HRRJK"]  | true                 | "port & rail terminal"         | "port & rail terminal"
    locationSampleMap["HRZAD"]                                 | locationSampleMap["HRKRK"]  | true                 | "port & rail terminal"         | "port"
    locationSampleMap["HRKRK"]                                 | locationSampleMap["HRZAD"]  | true                 | "port"                         | "port & rail terminal"
    locationSampleMap["HRZAG"]                                 | locationSampleMap["HRZAD"]  | true                 | "rail terminal"                | "port & rail terminal"
    locationSampleMap["HRZAG"]                                 | locationSampleMap["HRVZN"]  | true                 | "rail terminal"                | "rail terminal"
    locationSampleMap["HRZAG"]                                 | locationSampleMap["HRKRK"]  | false                | "rail terminal"                | "port"
    locationSampleMap["HRKRK"]                                 | locationSampleMap["HRZAG"]  | false                | "port"                         | "rail terminal"
    locationSampleMap["HRDKO"]                                 | locationSampleMap["HRZAG"]  | false                | "not port & not rail terminal" | "rail terminal"
    locationSampleMap["HRZAG"]                                 | locationSampleMap["HRDKO"]  | false                | "rail terminal"                | "not port & not rail terminal"
    locationSampleMap["HRMVN"]                                 | locationSampleMap["HRDKO"]  | false                | "not port & not rail terminal" | "not port & not rail terminal"
  }
}
