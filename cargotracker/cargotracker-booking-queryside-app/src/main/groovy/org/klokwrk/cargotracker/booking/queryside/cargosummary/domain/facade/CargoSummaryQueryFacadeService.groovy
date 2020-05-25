package org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.facade

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.lib.axon.cqrs.querygateway.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.metadata.report.ResponseMetaDataReport
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoSummaryQueryFacadeService {
  private final QueryGatewayAdapter queryGatewayAdapter

  CargoSummaryQueryFacadeService(QueryGateway queryGateway) {
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  OperationResponse<CargoSummaryQueryResponse> queryCargoSummary(OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryOperationRequest) {
    CargoSummaryQueryResponse cargoSummaryQueryResponse = queryGatewayAdapter.query(cargoSummaryQueryOperationRequest, CargoSummaryQueryResponse)
    return cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse)
  }

  protected OperationResponse<CargoSummaryQueryResponse> cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(CargoSummaryQueryResponse cargoSummaryQueryResponse) {
    ResponseMetaDataReport responseMetaDataReport = ResponseMetaDataReport.createBasicInfoMetaDataReport()
    return new OperationResponse<CargoSummaryQueryResponse>(payload: cargoSummaryQueryResponse, metaData: responseMetaDataReport.propertiesFiltered)
  }
}
