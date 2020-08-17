package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class BookCargoRequest {
  // TODO dmurat: validation - specify validation rules for properties.

  // Can be null. If specified, must be in uuid format.
  String aggregateIdentifier

  // Not null and must be in unLoCode format. After formal validation passes (1st level validation), it also must exist in the location registry (2nd level validation).
  String originLocation

  // Not null and must be in unLoCode format. After formal validation passes (1st level validation) , it also must exist in the location registry (2nd level validation).
  String destinationLocation
}
