package org.klokwrk.cargotracker.booking.commandside.cargobook.facade

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.domain.repository.LocationRegistryRepositoryService
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookFactoryService {
  private final LocationRegistryRepositoryService locationRegistryRepositoryService

  CargoBookFactoryService(LocationRegistryRepositoryService locationRegistryRepositoryService) {
    this.locationRegistryRepositoryService = locationRegistryRepositoryService
  }

  CargoBookCommand createCargoBookCommand(CargoBookRequest request) {
    // TODO dmurat: automate converting into commands
    Map cargoBookRequestProperties = request.properties

    String aggregateIdentifier = cargoBookRequestProperties.aggregateIdentifier ?: UUID.randomUUID().toString()
    Location originLocation = locationRegistryRepositoryService.findByUnLoCode(request.originLocation)
    Location destinationLocation = locationRegistryRepositoryService.findByUnLoCode(request.destinationLocation)

    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)
    return cargoBookCommand
  }

  CargoBookResponse createCargoBookResponse(CargoAggregate cargoAggregate) {
    // TODO dmurat: automate generating responses
    Map<String, ?> originLocationMap = [
        name: cargoAggregate.originLocation.name.name,
        nameInternationalized: cargoAggregate.originLocation.name.nameInternationalized,
        country: [
            name: cargoAggregate.originLocation.countryName.name,
            nameInternationalized: cargoAggregate.originLocation.countryName.nameInternationalized
        ],
        unLoCode: [
            code: cargoAggregate.originLocation.unLoCode.code,
            countryCode: cargoAggregate.originLocation.unLoCode.countryCode,
            locationCode: cargoAggregate.originLocation.unLoCode.locationCode
        ]
    ]

    Map<String, ?> destinationLocationMap = [
        name: cargoAggregate.destinationLocation.name.name,
        nameInternationalized: cargoAggregate.destinationLocation.name.nameInternationalized,
        country: [
            name: cargoAggregate.destinationLocation.countryName.name,
            nameInternationalized: cargoAggregate.destinationLocation.countryName.nameInternationalized
        ],
        unLoCode: [
            code: cargoAggregate.destinationLocation.unLoCode.code,
            countryCode: cargoAggregate.destinationLocation.unLoCode.countryCode,
            locationCode: cargoAggregate.destinationLocation.unLoCode.locationCode
        ]
    ]

    CargoBookResponse cargoBookResponse = new CargoBookResponse(aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap)
    return cargoBookResponse
  }
}
