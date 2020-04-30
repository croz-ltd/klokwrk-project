package net.croz.cargotracker.infrastructure.project.boundary.api.conversation

/**
 * Defines the basic format of messages exchanged over domain facade boundary.
 */
interface OperationMessage<P, M extends Map<String, ?>> {
  M getMetaData()
  P getPayload()
}
