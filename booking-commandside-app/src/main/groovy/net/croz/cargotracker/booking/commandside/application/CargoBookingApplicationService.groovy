package net.croz.cargotracker.booking.commandside.application

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.api.command.CargoBookResponse
import net.croz.cargotracker.booking.commandside.domain.model.CargoAggregate
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
class CargoBookingApplicationService {
  private CommandGateway commandGateway

  CargoBookingApplicationService(CommandGateway commandGateway) {
    this.commandGateway = commandGateway
  }

  CargoBookResponse cargoBook(CargoBookCommand cargoBookCommand) {
    CargoAggregate cargoAggregate = commandGateway.sendAndWait(cargoBookCommand)
    return cargoBookResponseFromCargoAggregate(cargoAggregate)
  }

  static CargoBookResponse cargoBookResponseFromCargoAggregate(CargoAggregate cargoAggregate) {
    return new CargoBookResponse(cargoAggregate.properties)
  }
}
