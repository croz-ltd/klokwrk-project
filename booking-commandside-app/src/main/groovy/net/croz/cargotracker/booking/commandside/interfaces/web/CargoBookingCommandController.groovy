package net.croz.cargotracker.booking.commandside.interfaces.web

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.application.CargoBookingApplicationService
import net.croz.cargotracker.booking.commandside.api.command.CargoBookResponse
import net.croz.cargotracker.booking.commandside.interfaces.web.dto.CargoBookWebRequest
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
    CargoBookResponse cargoAggregateResponse = cargoBookingApplicationService.cargoBook(cargoBookWebRequestToCargoBookCommand(cargoBookWebRequest))
    return cargoAggregateResponse
  }

  static CargoBookCommand cargoBookWebRequestToCargoBookCommand(CargoBookWebRequest cargoBookWebRequest) {
    new CargoBookCommand(
        aggregateIdentifier: cargoBookWebRequest.aggregateIdentifier ?: UUID.randomUUID().toString(),
        originLocation: cargoBookWebRequest.originLocation,
        destinationLocation: cargoBookWebRequest.destinationLocation
    )
  }
}
