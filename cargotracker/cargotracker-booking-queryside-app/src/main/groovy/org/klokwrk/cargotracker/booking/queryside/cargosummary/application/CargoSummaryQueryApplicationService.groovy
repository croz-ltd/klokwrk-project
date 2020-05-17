package org.klokwrk.cargotracker.booking.queryside.cargosummary.application

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.api.conversation.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.api.conversation.CargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.querygateway.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.conversation.metadata.ResponseMetaDataReport
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoSummaryQueryApplicationService {
  private final QueryGatewayAdapter queryGatewayAdapter

  CargoSummaryQueryApplicationService(QueryGateway queryGateway) {
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  OperationResponse<CargoSummaryQueryResponse> queryCargoSummary(OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryOperationRequest) {
    CargoSummaryQueryResponse cargoSummaryQueryResponse = queryGatewayAdapter.query(cargoSummaryQueryOperationRequest, CargoSummaryQueryResponse)
    return cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse)
  }

  OperationResponse<CargoSummaryQueryResponse> cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(CargoSummaryQueryResponse cargoSummaryQueryResponse) {
    ResponseMetaDataReport responseMetaDataReport = ResponseMetaDataReport.createBasicInfoMetaDataReport()
    return new OperationResponse<CargoSummaryQueryResponse>(payload: cargoSummaryQueryResponse, metaData: responseMetaDataReport.propertiesFiltered)
  }
}
