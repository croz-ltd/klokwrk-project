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
import org.klokwrk.lang.groovy.misc.InstantUtils
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

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

  static Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)
  static Instant currentInstantRounded = Instant.now(clock)
  static Instant currentInstantRoundedAndOneHour = currentInstantRounded + Duration.ofHours(1)
  static Instant currentInstantRoundedAndTwoHours = currentInstantRounded + Duration.ofHours(2)

  void "map constructor should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = new RouteSpecification(
        originLocation: locationSampleMap["HRRJK"], destinationLocation: locationSampleMap["NLRTM"],
        creationTime: currentInstantRounded, departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam
    )

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
    routeSpecification.creationTime == currentInstantRounded
    routeSpecification.departureEarliestTime == departureEarliestTimeParam
    routeSpecification.departureLatestTime == departureLatestTimeParam

    where:
    departureEarliestTimeParam      | departureLatestTimeParam
    currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour
  }

  void "map constructor should fail for invalid null params"() {
    when:
    new RouteSpecification(
        originLocation: originLocationParam, destinationLocation: destinationLocationParam,
        creationTime: creationTimeParam, departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    originLocationParam        | destinationLocationParam   | creationTimeParam     | departureEarliestTimeParam      | departureLatestTimeParam         | messagePartParam
    null                       | locationSampleMap["HRRJK"] | currentInstantRounded | currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | null                       | currentInstantRounded | currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["NLRTM"] | null                  | currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["NLRTM"] | currentInstantRounded | null                            | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["NLRTM"] | currentInstantRounded | currentInstantRoundedAndOneHour | null                             | "notNullValue"
  }

  void "map constructor should fail for location input params violating business rules"() {
    when:
    new RouteSpecification(
        originLocation: originLocationParam, destinationLocation: destinationLocationParam,
        creationTime: currentInstantRounded, departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours
    )

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

  void "map constructor should fail for departure time input params violating business rules"() {
    when:
    new RouteSpecification(
        originLocation: locationSampleMap["HRRJK"], destinationLocation: locationSampleMap["NLRTM"],
        creationTime: currentInstantRounded, departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam
    )

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    departureEarliestTimeParam                  | departureLatestTimeParam                    | violationCodeKeyParam
    currentInstantRounded                       | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRounded - Duration.ofHours(1) | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRoundedAndOneHour             | currentInstantRounded                       | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRoundedAndOneHour             | currentInstantRounded - Duration.ofHours(1) | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRounded + Duration.ofHours(5) | currentInstantRounded + Duration.ofHours(4) | "routeSpecification.departureEarliestTime.afterDepartureLatestTime"

    and:
    currentInstantRounded + Duration.ofMinutes(1)                         | currentInstantRoundedAndOneHour | "routeSpecification.departureEarliestTime.notInHours"
    currentInstantRounded + Duration.ofSeconds(1)                         | currentInstantRoundedAndOneHour | "routeSpecification.departureEarliestTime.notInHours"
    currentInstantRounded + Duration.ofMinutes(1) + Duration.ofSeconds(1) | currentInstantRoundedAndOneHour | "routeSpecification.departureEarliestTime.notInHours"

    and:
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofMinutes(1)                         | "routeSpecification.departureLatestTime.notInHours"
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofSeconds(1)                         | "routeSpecification.departureLatestTime.notInHours"
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofMinutes(1) + Duration.ofSeconds(1) | "routeSpecification.departureLatestTime.notInHours"
  }

  void "create() factory method should work for correct input params"() {
    when:
    RouteSpecification routeSpecification = RouteSpecification.create(
        locationSampleMap["HRRJK"], locationSampleMap["NLRTM"], departureEarliestTimeParam, departureLatestTimeParam, clock
    )

    then:
    routeSpecification.originLocation.unLoCode.code == "HRRJK"
    routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
    routeSpecification.creationTime == currentInstantRounded
    routeSpecification.departureEarliestTime == InstantUtils.roundUpInstantToTheHour(departureEarliestTimeParam)
    routeSpecification.departureLatestTime == InstantUtils.roundUpInstantToTheHour(departureLatestTimeParam)

    where:
    departureEarliestTimeParam                    | departureLatestTimeParam
    currentInstantRoundedAndOneHour               | currentInstantRoundedAndTwoHours
    currentInstantRoundedAndOneHour               | currentInstantRoundedAndOneHour
    currentInstantRounded + Duration.ofSeconds(1) | currentInstantRounded + Duration.ofSeconds(1) + Duration.ofHours(1)
    currentInstantRounded + Duration.ofSeconds(1) | currentInstantRounded + Duration.ofSeconds(1)
  }

  void "create() factory method should fail for invalid null input params"() {
    when:
    RouteSpecification.create(originLocationParam, destinationLocationParam, departureEarliestTimeParam, departureLatestTimeParam, clock)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    originLocationParam        | destinationLocationParam   | departureEarliestTimeParam      | departureLatestTimeParam         | messagePartParam
    null                       | locationSampleMap["HRRJK"] | currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | null                       | currentInstantRoundedAndOneHour | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["NLRTM"] | null                            | currentInstantRoundedAndTwoHours | "notNullValue"
    locationSampleMap["HRRJK"] | locationSampleMap["NLRTM"] | currentInstantRoundedAndOneHour | null                             | "notNullValue"
  }

  void "create() factory method should fail for location input params violating business rules"() {
    when:
    RouteSpecification.create(originLocationParam, destinationLocationParam, currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours, clock)

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

  void "create() factory method should work for departure time input params not rounded on hours"() {
    when:
    RouteSpecification routeSpecification = RouteSpecification.create(locationSampleMap["HRRJK"], locationSampleMap["NLRTM"], departureEarliestTimeParam, departureLatestTimeParam, clock)

    then:
    routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
    routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours

    where:
    departureEarliestTimeParam                                            | departureLatestTimeParam
    currentInstantRounded + Duration.ofMinutes(1)                         | currentInstantRoundedAndTwoHours
    currentInstantRounded + Duration.ofSeconds(1)                         | currentInstantRoundedAndTwoHours
    currentInstantRounded + Duration.ofMinutes(1) + Duration.ofSeconds(1) | currentInstantRoundedAndTwoHours

    and:
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofMinutes(1)
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofSeconds(1)
    currentInstantRoundedAndOneHour | currentInstantRoundedAndOneHour + Duration.ofMinutes(1) + Duration.ofSeconds(1)

    and:
    currentInstantRounded + Duration.ofMinutes(1)                         | currentInstantRoundedAndOneHour + Duration.ofMinutes(1)
    currentInstantRounded + Duration.ofSeconds(1)                         | currentInstantRoundedAndOneHour + Duration.ofSeconds(1)
    currentInstantRounded + Duration.ofMinutes(1) + Duration.ofSeconds(1) | currentInstantRoundedAndOneHour + Duration.ofMinutes(1) + Duration.ofSeconds(1)
  }
}
