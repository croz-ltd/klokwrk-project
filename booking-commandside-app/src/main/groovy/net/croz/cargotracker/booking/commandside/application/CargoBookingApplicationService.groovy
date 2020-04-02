package net.croz.cargotracker.booking.commandside.application

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.api.command.CargoBookResponse
import net.croz.cargotracker.booking.commandside.domain.model.CargoAggregate
import net.croz.cargotracker.shared.operation.OperationRequest
import net.croz.cargotracker.shared.operation.OperationResponse
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
class CargoBookingApplicationService {
  private CommandGateway commandGateway

  CargoBookingApplicationService(CommandGateway commandGateway) {
    this.commandGateway = commandGateway
  }

  OperationResponse<CargoBookResponse> cargoBook(OperationRequest<CargoBookCommand> cargoBookCommandOperationRequest) {
    GenericCommandMessage<CargoBookCommand> cargoBookCommandMessage = new GenericCommandMessage(cargoBookCommandOperationRequest.payload, cargoBookCommandOperationRequest.metaData)
    CargoAggregate cargoAggregate = commandGateway.sendAndWait(cargoBookCommandMessage)

    return cargoBookOperationResponseFromCargoAggregate(cargoAggregate)
  }

  static OperationResponse<CargoBookResponse> cargoBookOperationResponseFromCargoAggregate(CargoAggregate cargoAggregate) {
    return new OperationResponse(payload: new CargoBookResponse(cargoAggregate.properties))
  }
}
