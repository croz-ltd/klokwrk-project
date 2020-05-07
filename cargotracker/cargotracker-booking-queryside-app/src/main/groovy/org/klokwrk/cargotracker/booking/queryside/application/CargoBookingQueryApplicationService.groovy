package org.klokwrk.cargotracker.booking.queryside.application

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.ResponseMetaDataReport
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.boundary.api.queryside.conversation.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.boundary.api.queryside.conversation.CargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.querygateway.QueryGatewayAdapter
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingQueryApplicationService {
  private final QueryGatewayAdapter queryGatewayAdapter

  CargoBookingQueryApplicationService(QueryGateway queryGateway) {
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
