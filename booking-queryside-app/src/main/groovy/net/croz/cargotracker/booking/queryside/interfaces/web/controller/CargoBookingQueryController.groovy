package net.croz.cargotracker.booking.queryside.interfaces.web.controller

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.boundary.api.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.boundary.api.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.booking.queryside.application.CargoBookingQueryApplicationService
import net.croz.cargotracker.booking.queryside.interfaces.web.conversation.CargoSummaryQueryWebRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.MetaDataConstant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cargo-booking-query")
@CompileStatic
class CargoBookingQueryController {
  private final CargoBookingQueryApplicationService cargoBookingQueryApplicationService

  CargoBookingQueryController(CargoBookingQueryApplicationService cargoBookingQueryApplicationService) {
    this.cargoBookingQueryApplicationService = cargoBookingQueryApplicationService
  }

  @PostMapping("/cargo-summary-query")
  OperationResponse<CargoSummaryQueryResponse> cargoSummaryQuery(@RequestBody CargoSummaryQueryWebRequest webRequest, Locale locale) {
    OperationResponse<CargoSummaryQueryResponse> cargoSummary = cargoBookingQueryApplicationService.queryCargoSummary(createOperationRequest(webRequest, CargoSummaryQueryRequest, locale))
    return cargoSummary
  }

  /**
   * Creates {@link OperationRequest} from <code>webRequest</code> DTO.
   *
   * @param <P> Type of the {@link OperationRequest}'s payload.
   */
  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <P> OperationRequest<P> createOperationRequest(Object webRequest, Class<P> operationRequestPayloadType, Locale locale) {
    OperationRequest<P> operationRequest = new OperationRequest(
        payload: operationRequestPayloadType.newInstance(webRequest.properties),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    return operationRequest
  }
}
