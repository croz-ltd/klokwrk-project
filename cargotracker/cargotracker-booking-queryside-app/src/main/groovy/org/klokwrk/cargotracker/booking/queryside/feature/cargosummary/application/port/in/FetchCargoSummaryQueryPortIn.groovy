package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse

@CompileStatic
interface FetchCargoSummaryQueryPortIn {
  OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQuery(OperationRequest<FetchCargoSummaryQueryRequest> fetchCargoSummaryQueryOperationRequest)
}
