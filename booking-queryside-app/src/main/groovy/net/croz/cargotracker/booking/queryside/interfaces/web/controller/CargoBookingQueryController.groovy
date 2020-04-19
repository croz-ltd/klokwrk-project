package net.croz.cargotracker.booking.queryside.interfaces.web.controller

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.booking.queryside.application.CargoBookingQueryApplicationService
import net.croz.cargotracker.booking.queryside.interfaces.web.conversation.CargoSummaryQueryWebRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cargo-booking-query")
@CompileStatic
class CargoBookingQueryController {
  private CargoBookingQueryApplicationService cargoBookingQueryApplicationService

  CargoBookingQueryController(CargoBookingQueryApplicationService cargoBookingQueryApplicationService) {
    this.cargoBookingQueryApplicationService = cargoBookingQueryApplicationService
  }

  @PostMapping("/cargo-summary-query")
  OperationResponse<CargoSummaryQueryResponse> cargoSummaryQuery(@RequestBody CargoSummaryQueryWebRequest cargoSummaryQueryWebRequest) {
    def cargoSummary = cargoBookingQueryApplicationService.queryCargoSummary(cargoSummaryQueryWebRequestToCargoSummaryQueryOperationRequest(cargoSummaryQueryWebRequest))
    return cargoSummary
  }

  static OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryWebRequestToCargoSummaryQueryOperationRequest(CargoSummaryQueryWebRequest cargoSummaryWebRequest) {
    return new OperationRequest<CargoSummaryQueryRequest>(payload: new CargoSummaryQueryRequest(cargoSummaryWebRequest.properties))
  }
}
