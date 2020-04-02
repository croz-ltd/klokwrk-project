package net.croz.cargotracker.booking.queryside.interfaces.projection

import net.croz.cargotracker.booking.commandside.api.event.CargoBookedEvent
import net.croz.cargotracker.booking.queryside.domain.readmodel.CargoSummary
import net.croz.cargotracker.booking.queryside.domain.readmodel.CargoSummaryRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Transactional
class CargoSummaryProjector {

  private CargoSummaryRepository cargoSummaryRepository

  CargoSummaryProjector(CargoSummaryRepository cargoSummaryRepository) {
    this.cargoSummaryRepository = cargoSummaryRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {

    println "----- handling event: ${cargoBookedEvent.dump()}"
    cargoSummaryRepository.save(cargoSummaryFromCargoBookedEvent(cargoBookedEvent))
  }

  static CargoSummary cargoSummaryFromCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    CargoSummary cargoSummary = new CargoSummary(cargoBookedEvent.properties + [aggregateSequenceNumber: 0])
    return cargoSummary
  }
}
