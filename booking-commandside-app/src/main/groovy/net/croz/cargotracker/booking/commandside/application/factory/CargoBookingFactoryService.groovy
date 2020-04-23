package net.croz.cargotracker.booking.commandside.application.factory

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.api.axon.command.CargoBookCommand
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.application.repository.LocationRegistryRepositoryService
import net.croz.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import net.croz.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingFactoryService {
  private LocationRegistryRepositoryService locationRegistryRepositoryService

  CargoBookingFactoryService(LocationRegistryRepositoryService locationRegistryRepositoryService) {
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
