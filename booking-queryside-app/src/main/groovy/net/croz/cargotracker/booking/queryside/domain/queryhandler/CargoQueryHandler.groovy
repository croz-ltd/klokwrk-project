package net.croz.cargotracker.booking.queryside.domain.queryhandler

import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryResult
import net.croz.cargotracker.booking.queryside.domain.readmodel.CargoSummary
import net.croz.cargotracker.booking.queryside.domain.readmodel.CargoSummaryRepository
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service

@Service
class CargoQueryHandler {
  private CargoSummaryRepository cargoSummaryRepository

  CargoQueryHandler(CargoSummaryRepository cargoSummaryRepository) {
    this.cargoSummaryRepository = cargoSummaryRepository
  }

  @QueryHandler
  CargoSummaryResult handleCargoSummaryQuery(CargoSummaryQueryRequest cargoSummaryQuery) {
    CargoSummary cargoSummary = cargoSummaryRepository.findByAggregateIdentifier(cargoSummaryQuery.aggregateIdentifier)
    return new CargoSummaryResult(cargoSummary.properties)
  }
}
