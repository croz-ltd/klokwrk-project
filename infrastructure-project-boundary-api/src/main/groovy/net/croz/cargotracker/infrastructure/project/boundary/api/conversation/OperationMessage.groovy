package net.croz.cargotracker.infrastructure.project.boundary.api.conversation

interface OperationMessage<P, M extends Map<String, ?>> {
  M getMetaData()
  P getPayload()
}
