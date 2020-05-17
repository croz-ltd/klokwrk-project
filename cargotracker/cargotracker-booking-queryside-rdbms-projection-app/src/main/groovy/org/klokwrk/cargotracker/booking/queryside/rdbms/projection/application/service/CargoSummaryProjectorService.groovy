package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.application.service

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntityRepository
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.application.factory.CargoSummaryFactoryService
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectorService {
  private final CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository

  CargoSummaryProjectorService(CargoSummaryQueryEntityRepository cargoSummaryQueryEntityRepository) {
    this.cargoSummaryQueryEntityRepository = cargoSummaryQueryEntityRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    cargoSummaryQueryEntityRepository.save(CargoSummaryFactoryService.createCargoSummaryQueryEntity(cargoBookedEvent, domainEventMessage))
  }
}
