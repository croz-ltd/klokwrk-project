package net.croz.cargotracker.booking.queryside.domain.query

import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
class CargoSummaryQueryRequest {
  String aggregateIdentifier
}
