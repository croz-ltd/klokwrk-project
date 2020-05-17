package org.klokwrk.cargotracker.booking.commandside.cargobook.boundary

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoBookRequest {
  String aggregateIdentifier

  String originLocation
  String destinationLocation
}
