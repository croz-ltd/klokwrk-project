package net.croz.cargotracker.booking.queryside.application

import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryQuery
import net.croz.cargotracker.booking.queryside.domain.query.CargoSummaryResult
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service

@Service
class CargoBookingQueryApplicationService {
  private QueryGateway queryGateway

  CargoBookingQueryApplicationService(QueryGateway queryGateway) {
    this.queryGateway = queryGateway
  }

  CargoSummaryResult queryCargoSummary(CargoSummaryQuery cargoSummaryQuery) {
    CargoSummaryResult cargoSummaryResult = queryGateway.query(cargoSummaryQuery, CargoSummaryResult).join()
    return cargoSummaryResult
  }
}
