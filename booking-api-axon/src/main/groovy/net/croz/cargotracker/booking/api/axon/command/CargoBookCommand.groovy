package net.croz.cargotracker.booking.api.axon.command

import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.base.api.axon.command.BaseCreateCommand
import net.croz.cargotracker.booking.domain.model.Location
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
