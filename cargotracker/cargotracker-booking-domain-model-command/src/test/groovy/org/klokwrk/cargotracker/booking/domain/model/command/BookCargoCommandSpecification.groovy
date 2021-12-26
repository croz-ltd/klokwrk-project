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
package org.klokwrk.cargotracker.booking.domain.model.command

import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lang.groovy.misc.UUIDUtils
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class BookCargoCommandSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "NLRTM": Location.create("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "DEHAM": Location.create("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E"),
  ]

  static Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)
  static Instant currentInstantRounded = Instant.now(clock)
  static Instant currentInstantRoundedAndOneHour = currentInstantRounded + Duration.ofHours(1)
  static Instant currentInstantRoundedAndTwoHours = currentInstantRounded + Duration.ofHours(2)
  static Instant currentInstantRoundedAndThreeHours = currentInstantRounded + Duration.ofHours(3)
  static RouteSpecification validRouteSpecification = RouteSpecification.create(
      locationSampleMap["NLRTM"], locationSampleMap["DEHAM"], currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours, currentInstantRoundedAndThreeHours, clock
  )
  static CommodityInfo validCommodityInfo = CommodityInfo.create(CommodityType.DRY, 1000, null)

  void "map constructor should work for correct input params"() {
    when:
    CargoId cargoId = CargoId.createWithGeneratedIdentifier()
    BookCargoCommand bookCargoCommand = new BookCargoCommand(
        cargoId: cargoId,
        routeSpecification: validRouteSpecification,
        commodityInfo: validCommodityInfo
    )

    then:
    bookCargoCommand.cargoId
    UUIDUtils.checkIfRandomUuid(bookCargoCommand.cargoId.identifier)

    bookCargoCommand.routeSpecification.originLocation.unLoCode.code == "NLRTM"
    bookCargoCommand.routeSpecification.destinationLocation.unLoCode.code == "DEHAM"
    bookCargoCommand.routeSpecification.creationTime == currentInstantRounded
    bookCargoCommand.routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
    bookCargoCommand.routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours
    bookCargoCommand.routeSpecification.arrivalLatestTime == currentInstantRoundedAndThreeHours
  }

  void "map constructor should fail for null input params"() {
    when:
    new BookCargoCommand(cargoId: cargoIdParam, routeSpecification: routeSpecificationParam, commodityInfo: commodityInfoParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    cargoIdParam                            | routeSpecificationParam | commodityInfoParam | messagePartParam
    null                                    | validRouteSpecification | validCommodityInfo | "notNullValue"
    CargoId.createWithGeneratedIdentifier() | null                    | validCommodityInfo | "notNullValue"
    CargoId.createWithGeneratedIdentifier() | validRouteSpecification | null               | "notNullValue"
  }

  void "map constructor should fail when some of business rules are not satisfied"() {
    when:
    new BookCargoCommand(
        cargoId: cargoIdParam,
        routeSpecification: RouteSpecification.create(
            originLocationParam, destinationLocationParam,
            currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours,
            currentInstantRoundedAndThreeHours, clock
        ),
        commodityInfo: validCommodityInfo
    )

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    cargoIdParam                            | originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | locationSampleMap["NLRTM"] | "routeSpecification.originAndDestinationLocationAreEqual"
    CargoId.createWithGeneratedIdentifier() | locationSampleMap["NLRTM"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }
}
