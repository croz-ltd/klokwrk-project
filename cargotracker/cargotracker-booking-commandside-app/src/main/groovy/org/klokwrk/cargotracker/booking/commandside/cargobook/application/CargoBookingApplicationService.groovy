package org.klokwrk.cargotracker.booking.commandside.cargobook.application

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.gateway.CommandGateway
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.command.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.api.conversation.CargoBookRequest
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.api.conversation.CargoBookResponse
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.lib.axon.cqrs.commandgateway.CommandGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationResponse
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingApplicationService {
  private final CargoBookingFactoryService cargoBookingFactoryService
  private final CommandGatewayAdapter commandGatewayAdapter

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
