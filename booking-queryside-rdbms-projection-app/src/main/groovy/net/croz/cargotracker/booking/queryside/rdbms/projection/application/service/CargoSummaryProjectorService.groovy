package net.croz.cargotracker.booking.queryside.rdbms.projection.application.service

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.api.axon.event.CargoBookedEvent
import net.croz.cargotracker.booking.queryside.rdbms.domain.readmodel.CargoSummaryRepository
import net.croz.cargotracker.booking.queryside.rdbms.projection.application.factory.CargoSummaryFactoryService
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectorService {

  private CargoSummaryRepository cargoSummaryRepository

  CargoSummaryProjectorService(CargoSummaryRepository cargoSummaryRepository) {
    this.cargoSummaryRepository = cargoSummaryRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {

    println "----- handling event: ${cargoBookedEvent.dump()}"
    cargoSummaryRepository.save(CargoSummaryFactoryService.createCargoSummaryQueryEntity(cargoBookedEvent))
  }
}
