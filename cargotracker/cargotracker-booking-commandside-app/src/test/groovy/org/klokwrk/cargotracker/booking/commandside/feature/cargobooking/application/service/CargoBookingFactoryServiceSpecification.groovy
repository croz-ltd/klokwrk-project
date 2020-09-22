package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.out.remoting.InMemoryLocationRegistryService
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.FindLocationPortOut
import org.klokwrk.cargotracker.booking.domain.model.Location
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
    thrown(AssertionError)
  }

  void "createBookCargoCommand - should fail for invalid destinationLocation"() {
    given:
    BookCargoRequest bookCargoRequest = new BookCargoRequest(originLocation: "HRRJK", destinationLocation: "invalidDestination")

    when:
    cargoBookingFactoryService.createBookCargoCommand(bookCargoRequest)

    then:
    thrown(AssertionError)
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

  void "createBookCargoResponse - should throw for passed null"() {
    when:
    cargoBookingFactoryService.createBookCargoResponse(null)

    then:
    thrown(AssertionError)
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
