package net.croz.cargotracker.infrastructure.shared.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateVersion

class UpdateMyTestAggregateCommand {
  @TargetAggregateIdentifier
  String aggregateIdentifier

  @TargetAggregateVersion
  Long sequenceNumber

  String name
}
