package net.croz.cargotracker.booking.commandside.api.event

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed(noArg = true)
class CargoBookedEvent {
  String aggregateIdentifier
  String originLocation
  String destinationLocation
}
