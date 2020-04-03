package net.croz.cargotracker.booking.commandside.api.command

import net.croz.cargotracker.base.api.command.BaseCreateCommand
import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed(noArg = true)
class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  String originLocation
  String destinationLocation
}
