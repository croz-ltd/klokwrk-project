package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargosummary.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectorService {
  private final CargoSummaryJpaRepository cargoSummaryJpaRepository

  CargoSummaryProjectorService(CargoSummaryJpaRepository cargoSummaryJpaRepository) {
    this.cargoSummaryJpaRepository = cargoSummaryJpaRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    cargoSummaryJpaRepository.save(CargoSummaryFactory.createCargoSummaryJpaEntity(cargoBookedEvent, domainEventMessage))
  }
}
