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

import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.out.remoting.InMemoryLocationRegistryService
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.FindLocationPortOut
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import spock.lang.Specification

class CargoBookingFactoryServiceSpecification extends Specification {

  CargoBookingFactoryService cargoBookingFactoryService
  FindLocationPortOut findLocationPortOut

  void setup() {
    findLocationPortOut = new InMemoryLocationRegistryService()
    cargoBookingFactoryService = new CargoBookingFactoryService(findLocationPortOut)
  }

  void "createBookCargoCommand - should throw for passed null"() {
    when:
    cargoBookingFactoryService.createBookCargoCommand(null)

    then:
    thrown(AssertionError)
  }

  void "createBookCargoCommand - should fail for invalid originLocation"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(originLocation: "invalidOrigin", destinationLocation: "HRRJK")

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    CommandException commandException = thrown()
    commandException.violationInfo.severity == Severity.WARNING
    commandException.violationInfo.violationCode.code == "400"
    commandException.violationInfo.violationCode.codeMessage == "Bad Request"
    commandException.violationInfo.violationCode.codeAsText == "originLocationUnknown"
  }

  void "createBookCargoCommand - should fail for invalid destinationLocation"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(originLocation: "HRRJK", destinationLocation: "invalidDestination")

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    CommandException commandException = thrown()
    commandException.violationInfo.severity == Severity.WARNING
    commandException.violationInfo.violationCode.code == "400"
    commandException.violationInfo.violationCode.codeMessage == "Bad Request"
    commandException.violationInfo.violationCode.codeAsText == "destinationLocationUnknown"
  }

  void "createBookCargoCommand - should work for unspecified aggregate identifier"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    verifyAll(bookCargoCommand) {
      aggregateIdentifier
      originLocation.unLoCode.code == "HRRJK"
      destinationLocation.unLoCode.code == "HRZAG"
    }
  }

  void "createBookCargoCommand - should work for specified aggregate identifier"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    verifyAll(bookCargoCommand) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation.unLoCode.code == "HRRJK"
      destinationLocation.unLoCode.code == "HRZAG"
    }
  }

  void "createBookCargoCommand - should throw for specified aggregate identifier in invalid format"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: "invalid", originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    thrown(IllegalArgumentException)
  }

  void "createBookCargoResponse - should create expected response"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    Location myOriginLocation = findLocationPortOut.findByUnLoCode("HRRJK")
    Location myDestinationLocation = findLocationPortOut.findByUnLoCode("HRZAG")

    CargoAggregate cargoAggregate = new CargoAggregate(aggregateIdentifier: myAggregateIdentifier, originLocation: myOriginLocation, destinationLocation: myDestinationLocation)

    when:
    BookCargoResponse bookCargoResponse = cargoBookingFactoryService.createBookCargoResponse(cargoAggregate)

    then:
    verifyAll(bookCargoResponse) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation == [
          name: "Rijeka",
          nameInternationalized: "Rijeka",
          country: [
              name: "Hrvatska",
              nameInternationalized: "Hrvatska"
          ],
          unLoCode: [
              code: "HRRJK",
              countryCode: "HR",
              locationCode: "RJK"
          ]
      ]

      destinationLocation == [
          name: "Zagreb",
          nameInternationalized: "Zagreb",
          country: [
              name: "Hrvatska",
              nameInternationalized: "Hrvatska"
          ],
          unLoCode: [
              code: "HRZAG",
              countryCode: "HR",
              locationCode: "ZAG"
          ]
      ]
    }
  }
}
