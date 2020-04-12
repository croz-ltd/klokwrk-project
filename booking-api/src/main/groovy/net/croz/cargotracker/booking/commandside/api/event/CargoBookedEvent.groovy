package net.croz.cargotracker.booking.commandside.api.event

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed
import net.croz.cargotracker.booking.commandside.api.model.Location

@MapConstructorRelaxed(noArg = true)
class CargoBookedEvent {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
