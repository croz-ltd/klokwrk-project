package net.croz.cargotracker.booking.queryside.application

import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQuery
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryResult
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

  OperationResponse<CargoSummaryResult> queryCargoSummary(OperationRequest<CargoSummaryQuery> cargoSummaryQueryOperationRequest) {
    GenericMessage cargoSummaryQueryMessage = new GenericMessage(cargoSummaryQueryOperationRequest.payload, cargoSummaryQueryOperationRequest.metaData)
    CargoSummaryResult cargoSummaryQueryResponse = queryGateway.query(CargoSummaryQuery.name, cargoSummaryQueryMessage, ResponseTypes.instanceOf(CargoSummaryResult)).join()

    return cargoSummaryOperationResponseFromCargoSummaryQueryResponse(cargoSummaryQueryResponse)
  }

  static OperationResponse<CargoSummaryResult> cargoSummaryOperationResponseFromCargoSummaryQueryResponse(CargoSummaryResult cargoSummaryQueryResponse) {
    return new OperationResponse<CargoSummaryResult>(payload: cargoSummaryQueryResponse)
  }
}
