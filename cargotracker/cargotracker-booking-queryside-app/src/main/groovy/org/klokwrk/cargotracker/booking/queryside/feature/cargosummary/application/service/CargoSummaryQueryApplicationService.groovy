package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.service

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.querygateway.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.metadata.report.ResponseMetaDataReport
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoSummaryQueryApplicationService implements FetchCargoSummaryQueryPortIn {
  private final QueryGatewayAdapter queryGatewayAdapter

  CargoSummaryQueryApplicationService(QueryGateway queryGateway) {
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  @Override
  OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQuery(OperationRequest<FetchCargoSummaryQueryRequest> fetchCargoSummaryQueryOperationRequest) {
    FetchCargoSummaryQueryResponse fetchCargoSummaryQueryResponse = queryGatewayAdapter.query(fetchCargoSummaryQueryOperationRequest, FetchCargoSummaryQueryResponse)
    return fetchCargoSummaryQueryOperationResponseFromFetchCargoSummaryQueryResponse(fetchCargoSummaryQueryResponse)
  }

  protected OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQueryOperationResponseFromFetchCargoSummaryQueryResponse(FetchCargoSummaryQueryResponse fetchCargoSummaryQueryResponse) {
    ResponseMetaDataReport responseMetaDataReport = ResponseMetaDataReport.createBasicInfoMetaDataReport()
    return new OperationResponse<FetchCargoSummaryQueryResponse>(payload: fetchCargoSummaryQueryResponse, metaData: responseMetaDataReport.propertiesFiltered)
  }
}
