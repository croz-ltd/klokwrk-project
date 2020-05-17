package org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.queryhandler

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.api.conversation.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.api.conversation.CargoSummaryQueryResponse
import org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntity
import org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntityRepository
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerTrait
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationInfo
import org.springframework.stereotype.Service

@Service
@CompileStatic
class CargoSummaryQueryHandler implements QueryHandlerTrait {
  private final CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository

  CargoSummaryQueryHandler(CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository) {
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
