package org.klokwrk.cargotracker.lib.axon.api.command

import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Defines read-only properties for all Axon creational commands.
 */
interface BaseCreateCommand {
  @TargetAggregateIdentifier
  String getAggregateIdentifier()
}
