package org.klokwrk.cargotracker.lib.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateVersion

class UpdateMyTestAggregateWithoutExpectedIdentifiersCommand {
  @TargetAggregateIdentifier
  String unexpectedAggregateIdentifier

  @TargetAggregateVersion
  Long unexpectedSequenceNumber

  String name
}
