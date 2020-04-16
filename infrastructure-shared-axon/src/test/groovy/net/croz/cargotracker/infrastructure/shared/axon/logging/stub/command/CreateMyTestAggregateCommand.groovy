package net.croz.cargotracker.infrastructure.shared.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

class CreateMyTestAggregateCommand {
  @TargetAggregateIdentifier
  String aggregateIdentifier

  String name
}
