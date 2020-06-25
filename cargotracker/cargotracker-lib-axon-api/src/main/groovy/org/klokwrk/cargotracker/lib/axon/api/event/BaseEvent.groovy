package org.klokwrk.cargotracker.lib.axon.api.event

/**
 * Defines read-only properties for all Axon events.
 */
interface BaseEvent {
  String getAggregateIdentifier()
}
