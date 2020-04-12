package net.croz.cargotracker.booking.queryside.interfaces.web

import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.booking.queryside.application.CargoBookingQueryApplicationService
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQueryResponse
import net.croz.cargotracker.booking.queryside.interfaces.web.dto.CargoSummaryWebRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cargo-booking-query")
class CargoBookingQueryController {
  private CargoBookingQueryApplicationService cargoBookingQueryApplicationService

  CargoBookingQueryController(CargoBookingQueryApplicationService cargoBookingQueryApplicationService) {
    this.cargoBookingQueryApplicationService = cargoBookingQueryApplicationService
  }

  @PostMapping("/cargo-summary-query")
  CargoSummaryQueryResponse cargoSummaryQuery(@RequestBody CargoSummaryWebRequest cargoSummaryWebRequest) {
    CargoSummaryQueryResponse cargoSummary = cargoBookingQueryApplicationService.queryCargoSummary(cargoSummaryWebRequestToCargoSummaryQueryOperationRequest(cargoSummaryWebRequest)).payload
    return cargoSummary
  }

  static OperationRequest<CargoSummaryQueryRequest> cargoSummaryWebRequestToCargoSummaryQueryOperationRequest(CargoSummaryWebRequest cargoSummaryWebRequest) {
    return new OperationRequest<CargoSummaryQueryRequest>(payload: new CargoSummaryQueryRequest(cargoSummaryWebRequest.properties))
  }
}
