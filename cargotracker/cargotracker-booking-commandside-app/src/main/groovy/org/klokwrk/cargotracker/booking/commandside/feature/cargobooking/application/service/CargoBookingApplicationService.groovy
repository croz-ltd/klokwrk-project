package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.gateway.CommandGateway
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.commandgateway.CommandGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.stereotype.Service

import static org.assertj.core.api.Assertions.assertThat

@Service
@CompileStatic
class CargoBookingApplicationService implements BookCargoPortIn {
  private final CargoBookingFactoryService cargoBookingFactoryService
  private final CommandGatewayAdapter commandGatewayAdapter

  CargoBookingApplicationService(CommandGateway commandGateway, CargoBookingFactoryService cargoBookingFactoryService) {
    this.commandGatewayAdapter = new CommandGatewayAdapter(commandGateway)
    this.cargoBookingFactoryService = cargoBookingFactoryService
  }

  @Override
  OperationResponse<BookCargoResponse> bookCargo(OperationRequest<BookCargoRequest> bookCargoOperationRequest) {
    assertThat(bookCargoOperationRequest).as("bookCargoOperationRequest").isNotNull()

    // TODO dmurat: validation - implement validation of BookCargoRequest here.

    BookCargoCommand bookCargoCommand = cargoBookingFactoryService.createBookCargoCommand(bookCargoOperationRequest.payload)
    CargoAggregate cargoAggregate = commandGatewayAdapter.sendAndWait(bookCargoCommand, bookCargoOperationRequest.metaData)

    return new OperationResponse(payload: cargoBookingFactoryService.createBookCargoResponse(cargoAggregate))
  }
}
