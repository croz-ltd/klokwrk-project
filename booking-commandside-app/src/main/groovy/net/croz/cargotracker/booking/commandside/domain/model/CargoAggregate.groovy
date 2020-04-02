package net.croz.cargotracker.booking.commandside.domain.model

import net.croz.cargotracker.booking.commandside.api.command.CargoBookCommand
import net.croz.cargotracker.booking.commandside.api.event.CargoBookedEvent
import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@MapConstructorRelaxed(noArg = true)
@Aggregate
class CargoAggregate {
  @AggregateIdentifier
  String aggregateIdentifier

  String originLocation
  String destinationLocation

  CargoAggregate bookCargo(CargoBookCommand cargoBookCommand) {
    apply(cargoBookedEventFromCargoBookCommand(cargoBookCommand))
    return this
  }

  static CargoBookedEvent cargoBookedEventFromCargoBookCommand(CargoBookCommand cargoBookCommand) {
    return new CargoBookedEvent(aggregateIdentifier: cargoBookCommand.aggregateIdentifier, originLocation: cargoBookCommand.originLocation, destinationLocation: cargoBookCommand.destinationLocation)
  }

  @EventSourcingHandler
  onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    originLocation = cargoBookedEvent.originLocation
    destinationLocation = cargoBookedEvent.destinationLocation
  }
}
