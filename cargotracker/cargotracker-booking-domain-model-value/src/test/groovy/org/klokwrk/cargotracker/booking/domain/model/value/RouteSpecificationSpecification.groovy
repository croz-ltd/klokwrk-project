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

import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import spock.lang.Specification

class RouteSpecificationSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "HRDKO": Location.create("HRDKO", "Đakovo", "Croatia", "--3-----", "4518N 01824E", PortCapabilities.NO_PORT_CAPABILITIES),
      "HRKRK": Location.create("HRKRK", "Krk", "Croatia", "1-3-----", "4502N 01435E", PortCapabilities.SEA_PORT_CAPABILITIES),
      "HRMVN": Location.create("HRMVN", "Motovun", "Croatia", "--3-----", "4520N 01349E", PortCapabilities.NO_PORT_CAPABILITIES),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRVZN": Location.create("HRVZN", "Varaždin", "Croatia", "-23-----", "4618N 01620E", PortCapabilities.NO_PORT_CAPABILITIES),
      "HRZAD": Location.create("HRZAD", "Zadar", "Croatia", "1234----", "4407N 01514E", PortCapabilities.SEA_PORT_CAPABILITIES),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E", PortCapabilities.NO_PORT_CAPABILITIES),

      "NLRTM": Location.create("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "BEBRU": Location.create("BEBRU", "Brussel", "Belgium", "1234----", "5050N 00420E", PortCapabilities.RIVER_PORT_CAPABILITIES),
  ]

  void "map constructor should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = new RouteSpecification(originLocation: locationSampleMap["HRRJK"], destinationLocation: locationSampleMap["NLRTM"])

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
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
  }

  void "map constructor should fail for input params violating business rules"() {
    when:
    new RouteSpecification(originLocation: originLocationParam, destinationLocation: destinationLocationParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    Location.UNKNOWN_LOCATION  | locationSampleMap["NLRTM"] | "routeSpecification.unknownOriginLocation"
    locationSampleMap["HRRJK"] | Location.UNKNOWN_LOCATION  | "routeSpecification.unknownDestinationLocation"
    locationSampleMap["HRRJK"] | locationSampleMap["HRRJK"] | "routeSpecification.originAndDestinationLocationAreEqual"
    locationSampleMap["HRRJK"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
    locationSampleMap["HRZAG"] | locationSampleMap["HRRJK"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }

  void "create() factory method should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = RouteSpecification.create(locationSampleMap["HRRJK"], locationSampleMap["NLRTM"])

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
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
  }

  void "create() factory method should fail for input params violating business rules"() {
    when:
    RouteSpecification.create(originLocationParam, destinationLocationParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    Location.UNKNOWN_LOCATION  | locationSampleMap["NLRTM"] | "routeSpecification.unknownOriginLocation"
    locationSampleMap["HRRJK"] | Location.UNKNOWN_LOCATION  | "routeSpecification.unknownDestinationLocation"
    locationSampleMap["HRRJK"] | locationSampleMap["HRRJK"] | "routeSpecification.originAndDestinationLocationAreEqual"
    locationSampleMap["HRRJK"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }
}
