package net.croz.cargotracker.infrastructure.project.axon.logging.stub.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

class CreateMyTestAggregateCommand {
  @TargetAggregateIdentifier
  String aggregateIdentifier

  String name
}
