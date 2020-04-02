package net.croz.cargotracker.booking.commandside.api.command

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed
class CargoBookResponse {
  String aggregateIdentifier

  String originLocation
  String destinationLocation
}
