package net.croz.cargotracker.booking.queryside.application

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.api.open.shared.conversation.response.ResponseReport
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.infrastructure.axon.querygateway.QueryGatewayAdapter
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
    return cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse, cargoSummaryQueryOperationRequest)
  }

  static OperationResponse<CargoSummaryQueryResponse> cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(
      CargoSummaryQueryResponse cargoSummaryQueryResponse, OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryOperationRequest)
  {
    ResponseReport responseReport = ResponseReport.createBasicInfoReport().tap {
      locale = cargoSummaryQueryOperationRequest.locale
    }

    // TODO dmurat: make an web interceptor for resolving responseReport's titleText and titleDetailedText and for converting responseReport into HttpResponseReport
    return new OperationResponse<CargoSummaryQueryResponse>(payload: cargoSummaryQueryResponse, metaData: responseReport.propertiesFiltered)
  }
}
