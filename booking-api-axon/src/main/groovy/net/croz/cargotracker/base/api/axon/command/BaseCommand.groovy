package net.croz.cargotracker.base.api.axon.command

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateVersion

/**
 * Defines read-only properties for all Axon non-creational commands.
 */
interface BaseCommand {
  @TargetAggregateIdentifier
  String getAggregateIdentifier()

  @TargetAggregateVersion
  Long getAggregateSequenceNumber()
}
