package net.croz.cargotracker.booking.commandside.api.event

import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.booking.commandside.api.model.Location
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
class CargoBookedEvent {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
