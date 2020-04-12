package net.croz.cargotracker.base.api.axon.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Defines read-only properties for all Axon creational commands.
 */
interface BaseCreateCommand {
  @TargetAggregateIdentifier
  String getAggregateIdentifier()
}
