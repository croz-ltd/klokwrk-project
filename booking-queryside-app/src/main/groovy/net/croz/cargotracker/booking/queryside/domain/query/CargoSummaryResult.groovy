package net.croz.cargotracker.booking.queryside.domain.query

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed
class CargoSummaryResult {
  String aggregateIdentifier
  Long aggregateSequenceNumber

  String originLocation
  String destinationLocation
}
