package org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.gateway.CommandGateway
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.lib.axon.cqrs.commandgateway.CommandGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookFacadeService {
  private final CargoBookFactoryService cargoBookingFactoryService
  private final CommandGatewayAdapter commandGatewayAdapter

  CargoBookFacadeService(CommandGateway commandGateway, CargoBookFactoryService cargoBookingFactoryService) {
    this.commandGatewayAdapter = new CommandGatewayAdapter(commandGateway)
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  OperationResponse<CargoBookResponse> cargoBook(OperationRequest<CargoBookRequest> cargoBookOperationRequest) {
    CargoBookCommand cargoBookCommand = cargoBookingFactoryService.createCargoBookCommand(cargoBookOperationRequest.payload)
    CargoAggregate cargoAggregate = commandGatewayAdapter.sendAndWait(cargoBookCommand, cargoBookOperationRequest.metaData)

    return new OperationResponse(payload: cargoBookingFactoryService.createCargoBookResponse(cargoAggregate))
  }
}
