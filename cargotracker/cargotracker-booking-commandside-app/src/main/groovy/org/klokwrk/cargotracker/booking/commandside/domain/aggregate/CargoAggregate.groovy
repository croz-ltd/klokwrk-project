package org.klokwrk.cargotracker.booking.commandside.domain.aggregate

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler.CommandHandlerTrait
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.CommandException
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationCode
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationInfo
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate
import org.klokwrk.cargotracker.booking.axon.api.command.CargoBookCommand
import org.klokwrk.cargotracker.booking.axon.api.event.CargoBookedEvent

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class CargoAggregate implements CommandHandlerTrait {
  @AggregateIdentifier
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation

  CargoAggregate bookCargo(CargoBookCommand cargoBookCommand, MetaData metaData) {
    if (!cargoBookCommand.destinationLocation.canAcceptCargoFrom(cargoBookCommand.originLocation)) {
      ViolationCode violationCode = new ViolationCode(code: ViolationCode.BAD_REQUEST.code, codeAsText: "destinationLocationCannotAcceptCargo", codeMessage: ViolationCode.BAD_REQUEST.codeMessage)
      ViolationInfo violationInfo = new ViolationInfo(severity: ViolationInfo.BAD_REQUEST.severity, violationCode: violationCode)
      doThrow(new CommandException(violationInfo))
    }

    apply(cargoBookedEventFromCargoBookCommand(cargoBookCommand), metaData)
    return this
  }

  CargoBookedEvent cargoBookedEventFromCargoBookCommand(CargoBookCommand cargoBookCommand) {
    return new CargoBookedEvent(cargoBookCommand.properties)
  }

  @EventSourcingHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    originLocation = cargoBookedEvent.originLocation
    destinationLocation = cargoBookedEvent.destinationLocation
  }
}
