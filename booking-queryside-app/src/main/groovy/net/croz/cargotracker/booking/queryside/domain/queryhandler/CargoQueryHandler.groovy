package net.croz.cargotracker.booking.queryside.domain.queryhandler

import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.booking.queryside.rdbms.domain.readmodel.CargoSummary
import net.croz.cargotracker.booking.queryside.rdbms.domain.readmodel.CargoSummaryRepository
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service

@Service
class CargoQueryHandler {
  private CargoSummaryRepository cargoSummaryRepository

  CargoQueryHandler(CargoSummaryRepository cargoSummaryRepository) {
    this.cargoSummaryRepository = cargoSummaryRepository
  }

  @QueryHandler
  CargoSummaryQueryResponse handleCargoSummaryQuery(CargoSummaryQueryRequest cargoSummaryQuery) {
    CargoSummary cargoSummary = cargoSummaryRepository.findByAggregateIdentifier(cargoSummaryQuery.aggregateIdentifier)
    return new CargoSummaryQueryResponse(cargoSummary.properties)
  }
}
