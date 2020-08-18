package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor
@CompileStatic
class BookCargoResponse {
  String aggregateIdentifier

  Map<String, ?> originLocation
  Map<String, ?> destinationLocation
}
