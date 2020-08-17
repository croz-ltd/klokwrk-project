package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@RestController
@CompileStatic
class CargoBookingWebController {
  private final BookCargoPortIn bookCargoPortIn

  CargoBookingWebController(BookCargoPortIn bookCargoPortIn) {
    this.bookCargoPortIn = bookCargoPortIn
  }

  @PostMapping("/book-cargo")
  OperationResponse<BookCargoResponse> bookCargo(@RequestBody BookCargoWebRequest bookCargoWebRequest, HttpServletRequest httpServletRequest) {
    OperationResponse<BookCargoResponse> bookCargoResponse = bookCargoPortIn.bookCargo(CargoBookingWebAssembler.toBookCargoOperationRequest(bookCargoWebRequest, httpServletRequest))
    return bookCargoResponse
  }
}
