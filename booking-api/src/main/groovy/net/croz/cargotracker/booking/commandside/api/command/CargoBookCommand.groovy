package net.croz.cargotracker.booking.commandside.api.command

import net.croz.cargotracker.base.api.command.BaseCreateCommand

class CargoBookCommand implements BaseCreateCommand {
  String aggregateIdentifier

  String originLocation
  String destinationLocation
}
