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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.out.remoting.InMemoryLocationRegistryService
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.RouteSpecificationData
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.domain.model.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import spock.lang.Specification

class CargoBookingFactoryServiceSpecification extends Specification {

  CargoBookingFactoryService cargoBookingFactoryService
  LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut

  void setup() {
    locationByUnLoCodeQueryPortOut = new InMemoryLocationRegistryService()
    cargoBookingFactoryService = new CargoBookingFactoryService(locationByUnLoCodeQueryPortOut)
  }

  void "createBookCargoCommand - should throw for passed null"() {
    when:
    cargoBookingFactoryService.createBookCargoCommand(null)

    then:
    thrown(AssertionError)
  }

  void "createBookCargoCommand - should fail for invalid RouteSpecificationData"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        routeSpecification: new RouteSpecificationData(originLocation: originLocationParam, destinationLocation: destinationLocationParam)
    )

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoCommandRequest)

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

  void "createBookCargoCommand - should work for unspecified cargo identifier"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(routeSpecification: new RouteSpecificationData(originLocation: "HRRJK", destinationLocation: "NLRTM"))

    when:
    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoCommandRequest)

    then:
    verifyAll(bookCargoCommand) {
      aggregateIdentifier
      cargoId
      cargoId.identifier
      routeSpecification.originLocation.unLoCode.code == "HRRJK"
      routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
    }
  }

  void "createBookCargoCommand - should work for specified cargo identifier"() {
    given:
    String cargoIdentifier = UUID.randomUUID()
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: cargoIdentifier,
        routeSpecification: new RouteSpecificationData(originLocation: "HRRJK", destinationLocation: "NLRTM")
    )

    when:
    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoCommandRequest)

    then:
    verifyAll(bookCargoCommand) {
      aggregateIdentifier == cargoIdentifier
      cargoId.identifier == cargoIdentifier
      routeSpecification.originLocation.unLoCode.code == "HRRJK"
      routeSpecification.destinationLocation.unLoCode.code == "NLRTM"
    }
  }

  void "createBookCargoCommand - should throw for specified cargo identifier in invalid format"() {
    given:
    BookCargoCommandRequest bookCargoCommandRequest = new BookCargoCommandRequest(
        cargoIdentifier: "invalid",
        routeSpecification: new RouteSpecificationData(originLocation: "HRRJK", destinationLocation: "HRZAG")
    )

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoCommandRequest)

    then:
    thrown(AssertionError)
  }

  void "createBookCargoCommandResponse - should create expected response"() {
    given:
    String myCargoIdentifier = UUID.randomUUID()
    Location myOriginLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("HRRJK")
    Location myDestinationLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery("NLRTM")

    CargoAggregate cargoAggregate = new CargoAggregate(
        cargoId: CargoId.create(myCargoIdentifier),
        routeSpecification: new RouteSpecification(originLocation: myOriginLocation, destinationLocation: myDestinationLocation)
    )

    when:
    BookCargoCommandResponse bookCargoCommandResponse = cargoBookingFactoryService.createBookCargoCommandResponse(cargoAggregate)

    then:
    verifyAll(bookCargoCommandResponse) {
      cargoId == [
          identifier: myCargoIdentifier
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
          ]
      ]
    }
  }
}
