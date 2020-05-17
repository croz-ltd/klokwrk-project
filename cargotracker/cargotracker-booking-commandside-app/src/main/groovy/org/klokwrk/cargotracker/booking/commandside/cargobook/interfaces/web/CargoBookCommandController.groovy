package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.CargoBookResponse
import org.klokwrk.cargotracker.booking.commandside.cargobook.application.CargoBookFacadeService
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@RestController
@CompileStatic
class CargoBookCommandController {
  private final CargoBookFacadeService cargoBookingApplicationService

  CargoBookCommandController(CargoBookFacadeService cargoBookingApplicationService) {
    this.cargoBookingApplicationService = cargoBookingApplicationService
  }

  @PostMapping("/cargo-book")
  OperationResponse<CargoBookResponse> cargoBook(@RequestBody CargoBookWebRequest cargoBookWebRequest, HttpServletRequest httpServletRequest) {
    OperationResponse<CargoBookResponse> cargoBookResponse = cargoBookingApplicationService.cargoBook(CargoBookCommandAssembler.toCargoBookRequest(cargoBookWebRequest, httpServletRequest))
    return cargoBookResponse
  }
}
