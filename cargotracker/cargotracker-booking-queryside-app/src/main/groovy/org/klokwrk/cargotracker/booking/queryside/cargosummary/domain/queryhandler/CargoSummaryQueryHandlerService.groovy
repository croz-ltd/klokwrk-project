package org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.queryhandler

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.facade.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.cargosummary.domain.facade.CargoSummaryQueryResponse
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.domain.querymodel.CargoSummaryQueryEntity
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.domain.querymodel.CargoSummaryQueryEntityRepository
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerTrait
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.springframework.stereotype.Service

/**
 * Implements cargo summary query handling.
 * <p/>
 * Do note that there is no point for marking this class/method with <code>Transactional</code> annotations. Transaction is under control of Axon's <code>SpringTransactionManager</code> which does
 * not supports <code>Transactional</code> annotation. Rather <code>SpringTransactionManager</code> uses a single <code>TransactionDefinition</code> for all its message handlers.
 * <p/>
 * This is unfortunate since we are loosing capability to specify transaction attributes per class or method, and readOnly attribute will be quite nice to have. Fortunately, since this (and others)
 * query handler runs in standalone application, we can resolve this by configuring "global" Axon's transaction definition to be read-only.
 * <p/>
 * For more information and some resources, take a look at <code>axonTransactionManager</code> bean in <code>SpringBootConfig</code> class.
 */
@Service
@CompileStatic
class CargoSummaryQueryHandlerService implements QueryHandlerTrait {
  private final CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository

  CargoSummaryQueryHandlerService(CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository) {
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
