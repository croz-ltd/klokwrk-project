package net.croz.cargotracker.booking.queryside.domain.queryhandler

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.exception.QueryException
import net.croz.cargotracker.api.open.shared.exceptional.violation.ViolationInfo
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest
import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryResponse
import net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntity
import net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntityRepository
import net.croz.cargotracker.infrastructure.axon.messagehandler.QueryHandlerTrait
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoQueryHandler implements QueryHandlerTrait {
  private CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository

  CargoQueryHandler(CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository) {
    this.cargoSummaryQueryEntityRepository = cargoSummaryQueryEntityRepository
  }

  @QueryHandler
  CargoSummaryQueryResponse handleCargoSummaryQuery(CargoSummaryQueryRequest cargoSummaryQuery) {
    CargoSummaryQueryEntity cargoSummaryQueryEntity = cargoSummaryQueryEntityRepository.findByAggregateIdentifier(cargoSummaryQuery.aggregateIdentifier)

    if (!cargoSummaryQueryEntity) {
      doThrow(new QueryException(ViolationInfo.NOT_FOUND))
    }

    return new CargoSummaryQueryResponse(cargoSummaryQueryEntity.properties)
  }
}
