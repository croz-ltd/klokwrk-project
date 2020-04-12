package net.croz.cargotracker.booking.queryside.application

import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQuery
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryResult
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
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

  CargoSummaryResult queryCargoSummary(OperationRequest<CargoSummaryQuery> cargoSummaryQueryOperationRequest) {
    GenericMessage cargoSummaryQueryMessage = new GenericMessage(cargoSummaryQueryOperationRequest.payload, cargoSummaryQueryOperationRequest.metaData)
    CargoSummaryResult cargoSummaryResult = queryGateway.query(CargoSummaryQuery.name, cargoSummaryQueryMessage, ResponseTypes.instanceOf(CargoSummaryResult)).join()

    return cargoSummaryResult
  }
}
