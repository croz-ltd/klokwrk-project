/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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

import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
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

class CreateBookingOfferCommandSpecification extends Specification {
  static Map<String, Location> locationSampleMap = [
      "NLRTM": Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "DEHAM": Location.make("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRZAG": Location.make("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E"),
  ]

  static Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)
  static Instant currentInstantRounded = Instant.now(clock)
  static Instant currentInstantRoundedAndOneHour = currentInstantRounded + Duration.ofHours(1)
  static Instant currentInstantRoundedAndTwoHours = currentInstantRounded + Duration.ofHours(2)
  static Instant currentInstantRoundedAndThreeHours = currentInstantRounded + Duration.ofHours(3)
  static RouteSpecification validRouteSpecification = RouteSpecification.make(
      locationSampleMap["NLRTM"], locationSampleMap["DEHAM"], currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours, currentInstantRoundedAndThreeHours, clock
  )
  static CommodityInfo validCommodityInfo = CommodityInfo.make(CommodityType.DRY, 1000, null)
  static ContainerDimensionType validContainerDimensionType = ContainerDimensionType.DIMENSION_ISO_22

  void "map constructor should work for correct input params"() {
    when:
    BookingOfferId bookingOfferId = BookingOfferId.makeWithGeneratedIdentifier()
    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        bookingOfferId: bookingOfferId,
        routeSpecification: validRouteSpecification,
        commodityInfo: validCommodityInfo,
        containerDimensionType: validContainerDimensionType
    )

    then:
    createBookingOfferCommand.bookingOfferId
    UUIDUtils.checkIfRandomUuid(createBookingOfferCommand.bookingOfferId.identifier)

    createBookingOfferCommand.routeSpecification.originLocation.unLoCode.code == "NLRTM"
    createBookingOfferCommand.routeSpecification.destinationLocation.unLoCode.code == "DEHAM"
    createBookingOfferCommand.routeSpecification.creationTime == currentInstantRounded
    createBookingOfferCommand.routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
    createBookingOfferCommand.routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours
    createBookingOfferCommand.routeSpecification.arrivalLatestTime == currentInstantRoundedAndThreeHours
  }

  void "map constructor should fail for null input params"() {
    when:
    new CreateBookingOfferCommand(bookingOfferId: bookingOfferIdParam, routeSpecification: routeSpecificationParam, commodityInfo: commodityInfoParam, containerDimensionType: containerDimensionTypeParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    bookingOfferIdParam                            | routeSpecificationParam | commodityInfoParam | containerDimensionTypeParam | messagePartParam
    null                                           | validRouteSpecification | validCommodityInfo | validContainerDimensionType | "notNullValue"
    BookingOfferId.makeWithGeneratedIdentifier() | null                    | validCommodityInfo | validContainerDimensionType | "notNullValue"
    BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | null               | validContainerDimensionType | "notNullValue"
    BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | validCommodityInfo | null                        | "notNullValue"
  }

  void "map constructor should fail when some of business rules of routeSpecification are not satisfied"() {
    when:
    new CreateBookingOfferCommand(
        routeSpecification: RouteSpecification.make(
            originLocationParam, destinationLocationParam,
            currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours,
            currentInstantRoundedAndThreeHours, clock
        )
    )

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    originLocationParam        | destinationLocationParam   | violationCodeKeyParam
    locationSampleMap["NLRTM"] | locationSampleMap["NLRTM"] | "routeSpecification.originAndDestinationLocationAreEqual"
    locationSampleMap["NLRTM"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }
}
