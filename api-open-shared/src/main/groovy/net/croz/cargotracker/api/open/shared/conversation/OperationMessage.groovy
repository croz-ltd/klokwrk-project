package net.croz.cargotracker.api.open.shared.conversation

interface OperationMessage<P, M extends Map<String, ?>> {
  M getMetaData()
  P getPayload()
}
