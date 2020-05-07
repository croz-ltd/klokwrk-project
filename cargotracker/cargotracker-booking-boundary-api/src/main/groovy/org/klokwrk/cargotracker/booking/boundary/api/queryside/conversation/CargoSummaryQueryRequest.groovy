package org.klokwrk.cargotracker.booking.boundary.api.queryside.conversation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoSummaryQueryRequest {
  String aggregateIdentifier
}
