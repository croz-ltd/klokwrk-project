package net.croz.cargotracker.booking.queryside.interfaces.projection

import net.croz.cargotracker.booking.api.axon.event.CargoBookedEvent
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
    // TODO dmurat: automate populating persistent entity
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    String originLocation = cargoBookedEvent.originLocation.unLoCode.code
    String destinationLocation = cargoBookedEvent.destinationLocation.unLoCode.code
    Long aggregateSequenceNumber = 0

    CargoSummary cargoSummary = new CargoSummary(
        aggregateIdentifier: aggregateIdentifier, aggregateSequenceNumber: aggregateSequenceNumber, originLocation: originLocation, destinationLocation: destinationLocation
    )

    return cargoSummary
  }
}
