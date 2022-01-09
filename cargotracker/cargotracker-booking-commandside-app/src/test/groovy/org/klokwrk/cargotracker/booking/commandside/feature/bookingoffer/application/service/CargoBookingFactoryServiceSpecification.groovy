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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.service

import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.adapter.out.remoting.InMemoryLocationRegistryService
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CommodityInfoData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.RouteSpecificationData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferAggregate
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferCommodities
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class CargoBookingFactoryServiceSpecification extends Specification {

  static Clock clock = Clock.fixed(Instant.parse("2021-12-07T12:00:00Z"), ZoneOffset.UTC)
  static Instant currentInstantRounded = Instant.now(clock)
  static Instant currentInstantRoundedAndOneHour = currentInstantRounded + Duration.ofHours(1)
  static Instant currentInstantRoundedAndTwoHours = currentInstantRounded + Duration.ofHours(2)
  static Instant currentInstantRoundedAndThreeHours = currentInstantRounded + Duration.ofHours(3)
  static RouteSpecificationData validRouteSpecificationData = new RouteSpecificationData(
      originLocation: "HRRJK", destinationLocation: "NLRTM",
      departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
      arrivalLatestTime: currentInstantRoundedAndThreeHours
  )
  static CommodityInfoData validCommodityInfoData = new CommodityInfoData(commodityType: CommodityType.DRY, totalWeightInKilograms: 1000, requestedStorageTemperatureInCelsius: null)
  static String validContainerDimensionData = "DIMENSION_ISO_22"

  CargoBookingFactoryService cargoBookingFactoryService
  LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut

  void setup() {
    locationByUnLoCodeQueryPortOut = new InMemoryLocationRegistryService()
    cargoBookingFactoryService = new CargoBookingFactoryService(locationByUnLoCodeQueryPortOut, Optional.of(clock))
  }

  void "makeCreateBookingOfferCommand - should throw for passed null"() {
    when:
    cargoBookingFactoryService.makeCreateBookingOfferCommand(null)

    then:
    thrown(AssertionError)
  }

  void "makeCreateBookingOfferCommand - should fail for invalid locations in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        routeSpecification: new RouteSpecificationData(
            originLocation: originLocationParam, destinationLocation: destinationLocationParam,
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    originLocationParam | destinationLocationParam | violationCodeKeyParam
    "invalidOrigin"     | "HRRJK"                  | "routeSpecification.unknownOriginLocation"
    "HRRJK"             | "invalidOrigin"          | "routeSpecification.unknownDestinationLocation"
    "HRRJK"             | "HRRJK"                  | "routeSpecification.originAndDestinationLocationAreEqual"
    "HRRJK"             | "HRZAG"                  | "routeSpecification.cannotRouteCargoFromOriginToDestination"
  }

  void "makeCreateBookingOfferCommand - should fail for invalid departure instants in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        routeSpecification: new RouteSpecificationData(
            originLocation: "HRRJK", destinationLocation: "NLRTM",
            departureEarliestTime: departureEarliestTimeParam, departureLatestTime: departureLatestTimeParam,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    departureEarliestTimeParam                            | departureLatestTimeParam                    | violationCodeKeyParam
    currentInstantRounded                                 | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRounded - Duration.ofHours(1)           | currentInstantRoundedAndTwoHours            | "routeSpecification.departureEarliestTime.notInFuture"
    currentInstantRoundedAndOneHour                       | currentInstantRounded                       | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRoundedAndOneHour                       | currentInstantRounded - Duration.ofHours(1) | "routeSpecification.departureLatestTime.notInFuture"
    currentInstantRoundedAndOneHour + Duration.ofHours(1) | currentInstantRoundedAndOneHour             | "routeSpecification.departureEarliestTime.afterDepartureLatestTime"
  }

  void "makeCreateBookingOfferCommand - should fail for invalid arrival instant in RouteSpecificationData"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        routeSpecification: new RouteSpecificationData(
            originLocation: "HRRJK", destinationLocation: "NLRTM",
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: arrivalLatestTimeParam
        ),
        commodityInfo: validCommodityInfoData
    )

    when:
    cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    arrivalLatestTimeParam                                   | violationCodeKeyParam
    currentInstantRounded                                    | "routeSpecification.arrivalLatestTime.notInFuture"
    currentInstantRounded - Duration.ofMinutes(1)            | "routeSpecification.arrivalLatestTime.notInFuture"
    currentInstantRoundedAndTwoHours                         | "routeSpecification.arrivalLatestTime.beforeDepartureLatestTime"
    currentInstantRoundedAndTwoHours - Duration.ofMinutes(1) | "routeSpecification.arrivalLatestTime.beforeDepartureLatestTime"
  }

  void "makeCreateBookingOfferCommand - should work for unspecified cargo identifier"() {
    given:
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        routeSpecification: validRouteSpecificationData, commodityInfo: validCommodityInfoData, containerDimensionType: validContainerDimensionData
    )

    when:
    CreateBookingOfferCommand createBookingOfferCommand = cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

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
    String bookingOfferIdentifier = UUID.randomUUID()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        bookingOfferIdentifier: bookingOfferIdentifier,
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData,
        containerDimensionType: validContainerDimensionData
    )

    when:
    CreateBookingOfferCommand createBookingOfferCommand = cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    verifyAll(createBookingOfferCommand) {
      aggregateIdentifier == bookingOfferIdentifier
      bookingOfferId.identifier == bookingOfferIdentifier
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
        bookingOfferIdentifier: "invalid",
        routeSpecification: validRouteSpecificationData,
        commodityInfo: validCommodityInfoData
    )

    when:
    cargoBookingFactoryService.makeCreateBookingOfferCommand(createBookingOfferCommandRequest)

    then:
    thrown(AssertionError)
  }

  @SuppressWarnings("CodeNarc.MethodSize")
  void "makeCreateBookingOfferCommandResponse - should create expected response"() {
    given:
    String myBookingOfferIdentifier = UUID.randomUUID()
    Location myOriginLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("HRRJK")
    Location myDestinationLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("NLRTM")

    BookingOfferCommodities expectedBookingOfferCommodities = new BookingOfferCommodities()
    expectedBookingOfferCommodities.storeCommodity(new Commodity(
        containerType: ContainerType.TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(CommodityType.DRY, 1000),
        maxAllowedWeightPerContainer: Quantities.getQuantity(23_750, Units.KILOGRAM),
        maxRecommendedWeightPerContainer: Quantities.getQuantity(1000, Units.KILOGRAM),
        containerCount: 1
    ))

    BookingOfferAggregate bookingOfferAggregate = new BookingOfferAggregate(
        bookingOfferId: BookingOfferId.make(myBookingOfferIdentifier),
        routeSpecification: new RouteSpecification(
            originLocation: myOriginLocation, destinationLocation: myDestinationLocation,
            creationTime: currentInstantRounded,
            departureEarliestTime: currentInstantRoundedAndOneHour, departureLatestTime: currentInstantRoundedAndTwoHours,
            arrivalLatestTime: currentInstantRoundedAndThreeHours
        ),
        bookingOfferCommodities: expectedBookingOfferCommodities
    )

    when:
    CreateBookingOfferCommandResponse createBookingOfferCommandResponse = cargoBookingFactoryService.makeCreateBookingOfferCommandResponse(bookingOfferAggregate)

    then:
    verifyAll(createBookingOfferCommandResponse) {
      it.bookingOfferId == [
          identifier: myBookingOfferIdentifier
      ]

      it.routeSpecification == [
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

      it.bookingOfferCommodities == [
          commodityTypeToCommodityMap: expectedBookingOfferCommodities.commodityTypeToCommodityMap,
          totalCommodityWeight: Quantities.getQuantity(1000, Units.KILOGRAM),
          totalContainerCount: 1
      ]
    }
  }
}
