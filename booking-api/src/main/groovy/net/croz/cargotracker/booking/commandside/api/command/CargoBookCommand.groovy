package net.croz.cargotracker.booking.commandside.api.command

import net.croz.cargotracker.base.api.command.BaseCreateCommand
import net.croz.cargotracker.booking.commandside.api.model.Location
import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed(noArg = true)
class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  Location originLocation
  Location destinationLocation
}
