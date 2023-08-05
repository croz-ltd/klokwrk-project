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
package org.klokwrk.cargotracking.domain.model.command

import org.klokwrk.cargotracking.domain.model.command.data.CargoCommandData
import org.klokwrk.cargotracking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.domain.model.value.Location
import org.klokwrk.cargotracking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
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

  static Customer validStandardCustomer = Customer.make("${ UUID.randomUUID() }", CustomerType.STANDARD)
  static RouteSpecification validRouteSpecification = RouteSpecification.make(
      locationSampleMap["NLRTM"], locationSampleMap["DEHAM"], currentInstantRoundedAndOneHour, currentInstantRoundedAndTwoHours, currentInstantRoundedAndThreeHours, clock
  )
  static Commodity validCommodity = Commodity.make(CommodityType.DRY, 1000, null)
  static ContainerDimensionType validContainerDimensionType = ContainerDimensionType.DIMENSION_ISO_22
  static CargoCommandData validCargo = new CargoCommandData(commodity: validCommodity, containerDimensionType: validContainerDimensionType)
  static Collection<CargoCommandData> validCargos = [validCargo]

  void "map constructor should work for correct input params"() {
    when:
    BookingOfferId bookingOfferId = BookingOfferId.makeWithGeneratedIdentifier()
    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        customer: Customer.make("26d5f7d8-9ded-4ce3-b320-03a75f674f4e", CustomerType.STANDARD),
        bookingOfferId: bookingOfferId,
        routeSpecification: validRouteSpecification,
        cargos: validCargos
    )

    then:
    createBookingOfferCommand.bookingOfferId
    RandomUuidUtils.checkIfRandomUuidString(createBookingOfferCommand.bookingOfferId.identifier)

    createBookingOfferCommand.routeSpecification.originLocation.unLoCode.code == "NLRTM"
    createBookingOfferCommand.routeSpecification.destinationLocation.unLoCode.code == "DEHAM"
    createBookingOfferCommand.routeSpecification.creationTime == currentInstantRounded
    createBookingOfferCommand.routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
    createBookingOfferCommand.routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours
    createBookingOfferCommand.routeSpecification.arrivalLatestTime == currentInstantRoundedAndThreeHours
  }

  void "map constructor should fail for null input params"() {
    when:
    //noinspection GroovyAssignabilityCheck
    new CreateBookingOfferCommand(
        customer: customerParam,
        bookingOfferId: bookingOfferIdParam,
        routeSpecification: routeSpecificationParam,
        cargos: cargosParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messagePartParam)

    where:
    customerParam         | bookingOfferIdParam                          | routeSpecificationParam | cargosParam | messagePartParam
    null                  | BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | validCargos | "notNullValue"
    validStandardCustomer | null                                         | validRouteSpecification | validCargos | "notNullValue"
    validStandardCustomer | BookingOfferId.makeWithGeneratedIdentifier() | null                    | validCargos | "notNullValue"
    validStandardCustomer | BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | null        | "notNullValue"
    validStandardCustomer | BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | []          | "not(empty())"
    validStandardCustomer | BookingOfferId.makeWithGeneratedIdentifier() | validRouteSpecification | ["123"]     | "everyItem(instanceOf(CargoCommandData))"
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
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    originLocationParam        | destinationLocationParam   | resolvableMessageKeyParam
    locationSampleMap["NLRTM"] | locationSampleMap["NLRTM"] | "routeSpecification.originAndDestinationLocationAreEqual"
    locationSampleMap["NLRTM"] | locationSampleMap["HRZAG"] | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }
}
