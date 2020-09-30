package org.klokwrk.cargotracker.lib.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

class CreateMyTestAggregateWithoutExpectedIdentifierCommand {
  @TargetAggregateIdentifier
  String unexpectedAggregateIdentifier

  String name
}
