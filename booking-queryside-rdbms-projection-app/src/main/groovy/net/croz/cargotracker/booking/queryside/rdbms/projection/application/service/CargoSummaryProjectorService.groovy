package net.croz.cargotracker.booking.queryside.rdbms.projection.application.service

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.axon.api.event.CargoBookedEvent
import net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntityRepository
import net.croz.cargotracker.booking.queryside.rdbms.projection.application.factory.CargoSummaryFactoryService
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.EventHandler
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
