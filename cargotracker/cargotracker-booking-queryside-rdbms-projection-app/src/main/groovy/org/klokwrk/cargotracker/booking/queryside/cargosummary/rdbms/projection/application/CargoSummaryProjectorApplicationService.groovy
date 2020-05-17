package org.klokwrk.cargotracker.booking.queryside.cargosummary.rdbms.projection.application

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntityRepository
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectorApplicationService {
  private final CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository

  CargoSummaryProjectorApplicationService(CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository) {
    this.cargoSummaryQueryEntityRepository = cargoSummaryQueryEntityRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    cargoSummaryQueryEntityRepository.save(CargoSummaryFactory.createCargoSummaryQueryEntity(cargoBookedEvent, domainEventMessage))
  }
}
