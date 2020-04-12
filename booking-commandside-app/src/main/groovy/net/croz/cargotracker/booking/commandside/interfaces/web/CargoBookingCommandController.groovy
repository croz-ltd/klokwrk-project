package net.croz.cargotracker.booking.commandside.interfaces.web

import net.croz.cargotracker.booking.commandside.application.CargoBookingApplicationService
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.interfaces.web.dto.CargoBookWebRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
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
    CargoBookResponse cargoBookResponse = cargoBookingApplicationService.cargoBook(cargoBookWebRequestToCargoBookOperationRequest(cargoBookWebRequest)).payload
    return cargoBookResponse
  }

  static OperationRequest<CargoBookRequest> cargoBookWebRequestToCargoBookOperationRequest(CargoBookWebRequest cargoBookWebRequest) {
    return new OperationRequest(payload: new CargoBookRequest(cargoBookWebRequest.properties))
  }
}
