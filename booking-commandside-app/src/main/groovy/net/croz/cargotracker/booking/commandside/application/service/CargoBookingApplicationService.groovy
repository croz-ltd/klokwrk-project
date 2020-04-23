package net.croz.cargotracker.booking.commandside.application.service

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.api.axon.command.CargoBookCommand
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.application.factory.CargoBookingFactoryService
import net.croz.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import net.croz.cargotracker.infrastructure.project.axon.cqrs.commandgateway.CommandGatewayAdapter
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingApplicationService {
  private CargoBookingFactoryService cargoBookingFactoryService

  private CommandGatewayAdapter commandGatewayAdapter

  CargoBookingApplicationService(CommandGateway commandGateway, CargoBookingFactoryService cargoBookingFactoryService) {
    this.commandGatewayAdapter = new CommandGatewayAdapter(commandGateway)
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  OperationResponse<CargoBookResponse> cargoBook(OperationRequest<CargoBookRequest> cargoBookOperationRequest) {
    CargoBookCommand cargoBookCommand = cargoBookingFactoryService.createCargoBookCommand(cargoBookOperationRequest.payload)
    CargoAggregate cargoAggregate = commandGatewayAdapter.sendAndWait(cargoBookCommand, cargoBookOperationRequest.metaData)

    return new OperationResponse(payload: cargoBookingFactoryService.createCargoBookResponse(cargoAggregate))
  }
}
