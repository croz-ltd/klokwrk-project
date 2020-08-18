package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class FetchCargoSummaryQueryRequest {
  String aggregateIdentifier
}
