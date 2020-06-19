package org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade

import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.infrastructure.repository.InMemoryLocationRegistryRepositoryService
import org.klokwrk.cargotracker.booking.domain.model.Location
import spock.lang.Specification

class CargoBookFactoryServiceSpecification extends Specification {

  CargoBookFactoryService cargoBookFactoryService
  InMemoryLocationRegistryRepositoryService inMemoryLocationRegistryRepositoryService

  void setup() {
    inMemoryLocationRegistryRepositoryService = new InMemoryLocationRegistryRepositoryService()
    cargoBookFactoryService = new CargoBookFactoryService(inMemoryLocationRegistryRepositoryService)
  }

  @SuppressWarnings('GroovyResultOfObjectAllocationIgnored')
  void "should throw for nulls in constructor"() {
    when:
    new CargoBookFactoryService(null)

    then:
    thrown(IllegalArgumentException)
  }

  void "createCargoBookCommand - should throw for passed null"() {
    when:
    cargoBookFactoryService.createCargoBookCommand(null)

    then:
    thrown(IllegalArgumentException)
  }

  void "createCargoBookCommand - should fail for invalid originLocation"() {
    given:
    CargoBookRequest cargoBookRequest = new CargoBookRequest(originLocation: "invalidOrigin", destinationLocation: "HRRJK")

    when:
    cargoBookFactoryService.createCargoBookCommand(cargoBookRequest)

    then:
    thrown(AssertionError)
  }

  void "createCargoBookCommand - should fail for invalid destinationLocation"() {
    given:
    CargoBookRequest cargoBookRequest = new CargoBookRequest(originLocation: "HRRJK", destinationLocation: "invalidDestination")

    when:
    cargoBookFactoryService.createCargoBookCommand(cargoBookRequest)

    then:
    thrown(AssertionError)
  }

  void "createCargoBookCommand - should work for unspecified aggregate identifier"() {
    given:
    CargoBookRequest cargoBookRequest = new CargoBookRequest(originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    CargoBookCommand cargoBookCommand = cargoBookFactoryService.createCargoBookCommand(cargoBookRequest)

    then:
    verifyAll(cargoBookCommand) {
      aggregateIdentifier
      originLocation.unLoCode.code == "HRRJK"
      destinationLocation.unLoCode.code == "HRZAG"
    }
  }

  void "createCargoBookCommand - should work for specified aggregate identifier"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    CargoBookRequest cargoBookRequest = new CargoBookRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    CargoBookCommand cargoBookCommand = cargoBookFactoryService.createCargoBookCommand(cargoBookRequest)

    then:
    verifyAll(cargoBookCommand) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation.unLoCode.code == "HRRJK"
      destinationLocation.unLoCode.code == "HRZAG"
    }
  }

  void "createCargoBookCommand - should throw for specified aggregate identifier in invalid format"() {
    given:
    CargoBookRequest cargoBookRequest = new CargoBookRequest(aggregateIdentifier: "invalid", originLocation: "HRRJK", destinationLocation: "HRZAG")

    when:
    cargoBookFactoryService.createCargoBookCommand(cargoBookRequest)

    then:
    thrown(IllegalArgumentException)
  }

  void "createCargoBookResponse - should create expected response"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    Location myOriginLocation = inMemoryLocationRegistryRepositoryService.findByUnLoCode("HRRJK")
    Location myDestinationLocation = inMemoryLocationRegistryRepositoryService.findByUnLoCode("HRZAG")

    CargoAggregate cargoAggregate = new CargoAggregate(aggregateIdentifier: myAggregateIdentifier, originLocation: myOriginLocation, destinationLocation: myDestinationLocation)

    when:
    CargoBookResponse cargoBookResponse = cargoBookFactoryService.createCargoBookResponse(cargoAggregate)

    then:
    verifyAll(cargoBookResponse) {
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
