package net.croz.cargotracker.booking.boundary.api.queryside.conversation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoSummaryQueryResponse {
  String aggregateIdentifier
  Long aggregateSequenceNumber

  String originLocation
  String destinationLocation
}