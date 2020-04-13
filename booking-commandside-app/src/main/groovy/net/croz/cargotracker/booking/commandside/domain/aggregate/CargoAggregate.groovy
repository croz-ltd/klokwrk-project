package net.croz.cargotracker.booking.commandside.domain.aggregate

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.booking.api.axon.command.CargoBookCommand
import net.croz.cargotracker.booking.api.axon.event.CargoBookedEvent
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class CargoAggregate {
  @AggregateIdentifier
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation

  CargoAggregate bookCargo(CargoBookCommand cargoBookCommand) {
    apply(cargoBookedEventFromCargoBookCommand(cargoBookCommand))
    return this
  }

  static CargoBookedEvent cargoBookedEventFromCargoBookCommand(CargoBookCommand cargoBookCommand) {
    return new CargoBookedEvent(cargoBookCommand.properties)
  }

  @EventSourcingHandler
  onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    originLocation = cargoBookedEvent.originLocation
    destinationLocation = cargoBookedEvent.destinationLocation
  }
}
