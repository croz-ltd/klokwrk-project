package org.klokwrk.cargotracker.booking.axon.api.command

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.infrastructure.project.axon.api.command.BaseCreateCommand
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.cargotracker.booking.domain.model.Location

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
