package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CompileStatic
class CargoSummaryQueryWebController {
  private final FetchCargoSummaryQueryPortIn fetchCargoSummaryQueryPortIn

  CargoSummaryQueryWebController(FetchCargoSummaryQueryPortIn fetchCargoSummaryQueryPortIn) {
    this.fetchCargoSummaryQueryPortIn = fetchCargoSummaryQueryPortIn
  }

  @PostMapping("/fetch-cargo-summary")
  OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQuery(@RequestBody FetchCargoSummaryQueryWebRequest webRequest, Locale locale) {
    OperationResponse<FetchCargoSummaryQueryResponse> cargoSummary = fetchCargoSummaryQueryPortIn.fetchCargoSummaryQuery(createOperationRequest(webRequest, FetchCargoSummaryQueryRequest, locale))
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
