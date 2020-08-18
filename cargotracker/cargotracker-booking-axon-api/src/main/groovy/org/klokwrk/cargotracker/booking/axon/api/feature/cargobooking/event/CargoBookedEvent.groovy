package org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.klokwrk.cargotracker.lib.axon.api.event.BaseEvent
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoBookedEvent implements BaseEvent {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
