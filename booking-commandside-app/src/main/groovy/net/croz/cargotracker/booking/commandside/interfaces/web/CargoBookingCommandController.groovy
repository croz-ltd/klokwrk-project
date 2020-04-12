package net.croz.cargotracker.booking.commandside.interfaces.web

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.application.CargoBookingApplicationService
import net.croz.cargotracker.booking.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.interfaces.web.dto.CargoBookWebRequest
import net.croz.cargotracker.shared.operation.OperationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cargo-booking-command")
class CargoBookingCommandController {
  private CargoBookingApplicationService cargoBookingApplicationService

  CargoBookingCommandController(CargoBookingApplicationService cargoBookingApplicationService) {
    this.cargoBookingApplicationService = cargoBookingApplicationService
  }

  @PostMapping("/cargo-book")
  CargoBookResponse cargoBook(@RequestBody CargoBookWebRequest cargoBookWebRequest) {
    CargoBookResponse cargoBookResponse = cargoBookingApplicationService.cargoBook(cargoBookWebRequestToCargoBookCommandOperationRequest(cargoBookWebRequest)).payload
    return cargoBookResponse
  }

  static OperationRequest<CargoBookCommand> cargoBookWebRequestToCargoBookCommandOperationRequest(CargoBookWebRequest cargoBookWebRequest) {
    Map cargoBookWebRequestProperties = cargoBookWebRequest.properties
    cargoBookWebRequestProperties.aggregateIdentifier = cargoBookWebRequest.aggregateIdentifier ?: UUID.randomUUID().toString()

    return new OperationRequest(payload: new CargoBookCommand(cargoBookWebRequestProperties))
  }
}
