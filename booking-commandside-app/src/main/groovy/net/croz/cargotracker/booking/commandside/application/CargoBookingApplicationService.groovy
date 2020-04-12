package net.croz.cargotracker.booking.commandside.application

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.booking.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.domain.repository.LocationRegistryRepositoryService
import net.croz.cargotracker.shared.operation.OperationRequest
import net.croz.cargotracker.shared.operation.OperationResponse
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
class CargoBookingApplicationService {
  private CommandGateway commandGateway
  private LocationRegistryRepositoryService locationRegistryService

  CargoBookingApplicationService(CommandGateway commandGateway, LocationRegistryRepositoryService locationRegistryRepositoryService) {
    this.commandGateway = commandGateway
    this.locationRegistryService = locationRegistryRepositoryService
  }

  OperationResponse<CargoBookResponse> cargoBook(OperationRequest<CargoBookRequest> cargoBookOperationRequest) {
    // TODO dmurat: automate converting into commands
    Map cargoBookRequestProperties = cargoBookOperationRequest.payload.properties

    String aggregateIdentifier = cargoBookRequestProperties.aggregateIdentifier ?: UUID.randomUUID().toString()
    Location originLocation = locationRegistryService.findByUnLoCode(cargoBookOperationRequest.payload.originLocation)
    Location destinationLocation = locationRegistryService.findByUnLoCode(cargoBookOperationRequest.payload.destinationLocation)

    CargoBookCommand cargoBookCommand = new CargoBookCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)

    GenericCommandMessage<CargoBookCommand> cargoBookCommandMessage = new GenericCommandMessage(cargoBookCommand, cargoBookOperationRequest.metaData)
    CargoAggregate cargoAggregate = commandGateway.sendAndWait(cargoBookCommandMessage)

    return cargoBookOperationResponseFromCargoAggregate(cargoAggregate)
  }

  static OperationResponse<CargoBookResponse> cargoBookOperationResponseFromCargoAggregate(CargoAggregate cargoAggregate) {
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

    return new OperationResponse(
        payload: new CargoBookResponse(
            aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap
        )
    )
  }
}
