package org.klokwrk.cargotracker.booking.commandside.domain.aggregate

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.CommandHandlerTrait
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class CargoAggregate implements CommandHandlerTrait {
  static final String VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO = "destinationLocationCannotAcceptCargo"

  @AggregateIdentifier
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.ALWAYS)
  CargoAggregate bookCargo(BookCargoCommand bookCargoCommand, MetaData metaData) {
    if (!bookCargoCommand.destinationLocation.canAcceptCargoFrom(bookCargoCommand.originLocation)) {
      doThrow(new CommandException(ViolationInfo.createForBadRequestWithCustomCodeAsText(VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO)))
    }

    apply(cargoBookedEventFromBookCargoCommand(bookCargoCommand), metaData)
    return this
  }

  CargoBookedEvent cargoBookedEventFromBookCargoCommand(BookCargoCommand bookCargoCommand) {
    return new CargoBookedEvent(bookCargoCommand.properties)
  }

  @EventSourcingHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    originLocation = cargoBookedEvent.originLocation
    destinationLocation = cargoBookedEvent.destinationLocation
  }
}
