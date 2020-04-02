package net.croz.cargotracker.booking.commandside.api.event

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed
class CargoBookedEvent {
  String aggregateIdentifier
  String originLocation
  String destinationLocation
}
