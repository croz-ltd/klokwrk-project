package net.croz.cargotracker.booking.queryside.application

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.response.ResponseReport
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.infrastructure.project.axon.cqrs.querygateway.QueryGatewayAdapter
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoBookingQueryApplicationService {
  private QueryGatewayAdapter queryGatewayAdapter

  CargoBookingQueryApplicationService(QueryGateway queryGateway) {
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  OperationResponse<CargoSummaryQueryResponse> queryCargoSummary(OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryOperationRequest) {
    CargoSummaryQueryResponse cargoSummaryQueryResponse = queryGatewayAdapter.query(cargoSummaryQueryOperationRequest, CargoSummaryQueryResponse)
    return cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse)
  }

  static OperationResponse<CargoSummaryQueryResponse> cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(CargoSummaryQueryResponse cargoSummaryQueryResponse) {
    ResponseReport responseReport = ResponseReport.createBasicInfoReport()
    return new OperationResponse<CargoSummaryQueryResponse>(payload: cargoSummaryQueryResponse, metaData: responseReport.propertiesFiltered)
  }
}
