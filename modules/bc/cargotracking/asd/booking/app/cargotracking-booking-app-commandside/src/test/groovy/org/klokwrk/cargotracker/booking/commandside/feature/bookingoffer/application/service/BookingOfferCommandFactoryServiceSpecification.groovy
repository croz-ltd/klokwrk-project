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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.service

import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.adapter.out.remoting.InMemoryLocationRegistryService
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferAggregate
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferCargos
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.domain.model.value.Location
import org.klokwrk.cargotracking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.booking.out.customer.adapter.InMemoryCustomerRegistryService
import org.klokwrk.cargotracker.booking.out.customer.port.CustomerByUserIdPortOut
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class BookingOfferCommandFactoryServiceSpecification extends Specification {

  static Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)
  static Instant currentInstantRounded = Instant.now(clock)
  static Instant currentInstantRoundedAndOneHour = currentInstantRounded + Duration.ofHours(1)
  static Instant currentInstantRoundedAndTwoHours = currentInstantRounded + Duration.ofHours(2)
  static Instant currentInstantRoundedAndThreeHours = currentInstantRounded + Duration.ofHours(3)
  static RouteSpecificationRequestData validRouteSpecificationRequestData = new RouteSpecificationRequestData(
      originLocation: "HRRJK", destinationLocation: "NLRTM",
      departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
      arrivalLatestTime: currentInstantRoundedAndThreeHours
  )
  static String validContainerDimensionRequestData = "DIMENSION_ISO_22"
  static CargoRequestData validCargoRequestData = new CargoRequestData(
      commodityType: CommodityType.DRY.name(),
      commodityWeight: 1000.kg, commodityRequestedStorageTemperature: null, containerDimensionType: validContainerDimensionRequestData
  )

  BookingOfferCommandFactoryService bookingOfferCommandFactoryService
  LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut
  CustomerByUserIdPortOut customerByUserIdPortOut

  void setup() {
    locationByUnLoCodeQueryPortOut = new InMemoryLocationRegistryService()
    customerByUserIdPortOut = new InMemoryCustomerRegistryService()
    bookingOfferCommandFactoryService = new BookingOfferCommandFactoryService(customerByUserIdPortOut, locationByUnLoCodeQueryPortOut, Optional.of(clock))
  }

  void "makeCreateBookingOfferCommand - should throw for passed null"() {
    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(null)

    then:
    thrown(AssertionError)
  }

  void "makeCreateBookingOfferCommand - should fail for unknown customer"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(userId: "unknownIdentifier")

    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.resolvableMessageKey == "customerByUserIdPortOut.findCustomerByUserId.notFound"
  }

  void "makeCreateBookingOfferCommand - should fail for invalid locations in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        cargos: [validCargoRequestData]
    )

    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    originLocationParam | destinationLocationParam | resolvableMessageKeyParam
    "invalidOrigin"     | "HRRJK"                  | "routeSpecification.unknownOriginLocation"
    "HRRJK"             | "invalidOrigin"          | "routeSpecification.unknownDestinationLocation"
    "HRRJK"             | "HRRJK"                  | "routeSpecification.originAndDestinationLocationAreEqual"
    "HRRJK"             | "HRZAG"                  | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }

  void "makeCreateBookingOfferCommand - should fail for invalid departure instants in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "HRRJK", destinationLocation: "NLRTM",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        cargos: [validCargoRequestData]
    )

    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    departureEarliestTimeParam                            | departureLatestTimeParam                    | resolvableMessageKeyParam
    currentInstantRounded                                 | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRounded - Duration.ofHours(1)           | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRoundedAndOneHour                       | currentInstantRounded                       | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRoundedAndOneHour                       | currentInstantRounded - Duration.ofHours(1) | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRoundedAndOneHour + Duration.ofHours(1) | currentInstantRoundedAndOneHour             | "routeSpecification.departureEarliestTime.afterDepartureLatestTime"
  }

  void "makeCreateBookingOfferCommand - should fail for invalid arrival instant in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "HRRJK", destinationLocation: "NLRTM",
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: arrivalLatestTimeParam
        ),
        cargos: [validCargoRequestData]
    )

    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    arrivalLatestTimeParam                                   | resolvableMessageKeyParam
    currentInstantRounded                                    | "routeSpecification.arrivalLatestTime.notInFuture"
    currentInstantRounded - Duration.ofMinutes(1)            | "routeSpecification.arrivalLatestTime.notInFuture"
    currentInstantRoundedAndTwoHours                         | "routeSpecification.arrivalLatestTime.beforeDepartureLatestTime"
    currentInstantRoundedAndTwoHours - Duration.ofMinutes(1) | "routeSpecification.arrivalLatestTime.beforeDepartureLatestTime"
  }

  void "makeCreateBookingOfferCommand - should work for unspecified cargo identifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [validCargoRequestData]
    )

    when:
    CreateBookingOfferCommand createBookingOfferCommand = bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    verifyAll(createBookingOfferCommand) {
      aggregateIdentifier
      bookingOfferId
      bookingOfferId.identifier
      routeSpecification.originLocation.unLoCode.code == "HRRJK"
      routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
      routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
      routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours
      routeSpecification.arrivalLatestTime == currentInstantRoundedAndThreeHours
    }
  }

  void "makeCreateBookingOfferCommand - should work for specified cargo identifier"() {
    given:
    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        bookingOfferId: myBookingOfferId,
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [validCargoRequestData]
    )

    when:
    CreateBookingOfferCommand createBookingOfferCommand = bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    verifyAll(createBookingOfferCommand) {
      aggregateIdentifier == myBookingOfferId
      bookingOfferId.identifier == myBookingOfferId
      routeSpecification.originLocation.unLoCode.code == "HRRJK"
      routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
      routeSpecification.departureEarliestTime == currentInstantRoundedAndOneHour
      routeSpecification.departureLatestTime == currentInstantRoundedAndTwoHours
      routeSpecification.arrivalLatestTime == currentInstantRoundedAndThreeHours
    }
  }

  void "makeCreateBookingOfferCommand - should throw for specified cargo identifier in invalid format"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracker.com",
        bookingOfferId: "invalid",
        routeSpecification: validRouteSpecificationRequestData,
        cargos: [validCargoRequestData]
    )

    when:
    bookingOfferCommandFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    thrown(DomainException)
  }

  void "makeCreateBookingOfferCommandResponse - should create expected response"() {
    given:
    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    Location myOriginLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("HRRJK")
    Location myDestinationLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("NLRTM")

    BookingOfferCargos expectedBookingOfferCargos = new BookingOfferCargos()
    expectedBookingOfferCargos.storeCargoCollectionAddition([Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 1000))])

    BookingOfferAggregate bookingOfferAggregate = new BookingOfferAggregate(
        customer: Customer.make("26d5f7d8-9ded-4ce3-b320-03a75f674f4e", CustomerType.STANDARD),
        bookingOfferId: BookingOfferId.make(myBookingOfferId),
        routeSpecification: new RouteSpecification(
            originLocation: myOriginLocation, destinationLocation: myDestinationLocation,
            creationTime: currentInstantRounded,
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        bookingOfferCargos: expectedBookingOfferCargos
    )

    when:
    CreateBookingOfferCommandResponse createBookingOfferCommandResponse = bookingOfferCommandFactoryService.makeCreateBookingOfferCommandResponse(bookingOfferAggregate)

    then:
    verifyAll(createBookingOfferCommandResponse) {
      customer == [
          customerId: "26d5f7d8-9ded-4ce3-b320-03a75f674f4e",
          customerType: "STANDARD"
      ]

      bookingOfferId == [
          identifier: myBookingOfferId
      ]

      routeSpecification == [
          originLocation: [
              name: "Rijeka",
              countryName: "Croatia",
              unLoCode: [
                  code: [
                      encoded: "HRRJK",
                      countryCode: "HR",
                      locationCode: "RJK"
                  ],
                  coordinates: [
                      encoded: "4520N 01424E",
                      latitudeInDegrees: 45.33,
                      longitudeInDegrees: 14.40
                  ],
                  function: [
                      encoded: "1234----",
                      isPort: true,
                      isRailTerminal: true,
                      isRoadTerminal: true,
                      isAirport: true,
                      isPostalExchangeOffice: false,
                      isBorderCrossing: false
                  ]
              ],
              portCapabilities: ["CONTAINER_PORT", "SEA_PORT"]
          ],

          destinationLocation: [
              name: "Rotterdam",
              countryName: "Netherlands",
              unLoCode: [
                  code: [
                      encoded: "NLRTM",
                      countryCode: "NL",
                      locationCode: "RTM"
                  ],
                  coordinates: [
                      encoded: "5155N 00430E",
                      latitudeInDegrees: 51.92,
                      longitudeInDegrees: 4.50
                  ],
                  function: [
                      encoded: "12345---",
                      isPort: true,
                      isRailTerminal: true,
                      isRoadTerminal: true,
                      isAirport: true,
                      isPostalExchangeOffice: true,
                      isBorderCrossing: false
                  ]
              ],
              portCapabilities: ["CONTAINER_PORT", "SEA_PORT"]
          ],

          departureEarliestTime: currentInstantRoundedAndOneHour,
          departureLatestTime: currentInstantRoundedAndTwoHours,
          arrivalLatestTime: currentInstantRoundedAndThreeHours
      ]

      verifyAll(bookingOfferCargos) {
        bookingOfferCargoCollection.size() == expectedBookingOfferCargos.bookingOfferCargoCollection.size()
        bookingOfferCargoCollection.containsAll(expectedBookingOfferCargos.bookingOfferCargoCollection)

        totalCommodityWeight == 1000.kg
        totalContainerTeuCount == 1.00G
      }
    }
  }
}
