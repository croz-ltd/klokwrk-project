package net.croz.cargotracker.booking.commandside.conversation

import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
class CargoBookRequest {
  String aggregateIdentifier

  String originLocation
  String destinationLocation
}
