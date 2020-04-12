package net.croz.cargotracker.booking.queryside.application

import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service

@Service
class CargoBookingQueryApplicationService {
  private QueryGateway queryGateway

  CargoBookingQueryApplicationService(QueryGateway queryGateway) {
    this.queryGateway = queryGateway
  }

  OperationResponse<CargoSummaryQueryResponse> queryCargoSummary(OperationRequest<CargoSummaryQueryRequest> cargoSummaryQueryOperationRequest) {
    GenericMessage cargoSummaryQueryMessage = new GenericMessage(cargoSummaryQueryOperationRequest.payload, cargoSummaryQueryOperationRequest.metaData)
    CargoSummaryQueryResponse cargoSummaryQueryResponse = queryGateway.query(CargoSummaryQueryRequest.name, cargoSummaryQueryMessage, ResponseTypes.instanceOf(CargoSummaryQueryResponse)).join()

    return cargoSummaryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse)
  }

  static OperationResponse<CargoSummaryQueryResponse> cargoSummaryOperationResponseFromCargoSummaryQueryResponse(CargoSummaryQueryResponse cargoSummaryQueryResponse) {
    return new OperationResponse<CargoSummaryQueryResponse>(payload: cargoSummaryQueryResponse)
  }
}
