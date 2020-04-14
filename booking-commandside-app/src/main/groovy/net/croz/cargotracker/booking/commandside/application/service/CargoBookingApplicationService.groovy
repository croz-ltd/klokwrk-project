package net.croz.cargotracker.booking.commandside.application.service

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.booking.api.axon.command.CargoBookCommand
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.application.factory.CargoBookingFactoryService
import net.croz.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingApplicationService {
  private CommandGateway commandGateway
  private CargoBookingFactoryService cargoBookingFactoryService

  CargoBookingApplicationService(CommandGateway commandGateway, CargoBookingFactoryService cargoBookingFactoryService) {
    this.commandGateway = commandGateway
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  OperationResponse<CargoBookResponse> cargoBook(OperationRequest<CargoBookRequest> cargoBookOperationRequest) {
    CargoBookCommand cargoBookCommand = cargoBookingFactoryService.createCargoBookCommand(cargoBookOperationRequest.payload)

    GenericCommandMessage<CargoBookCommand> cargoBookCommandMessage = new GenericCommandMessage(cargoBookCommand, cargoBookOperationRequest.metaData)
    CargoAggregate cargoAggregate = commandGateway.sendAndWait(cargoBookCommandMessage)

    return new OperationResponse(payload: cargoBookingFactoryService.createCargoBookResponse(cargoAggregate))
  }
}
