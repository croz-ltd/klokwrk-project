package org.klokwrk.cargotracker.lib.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

class CreateMyTestAggregateCommand {
  @TargetAggregateIdentifier
  String aggregateIdentifier

  String name
}
