package org.klokwrk.cargotracker.booking.axon.api.command

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.klokwrk.cargotracker.lib.axon.api.command.BaseCreateCommand

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
