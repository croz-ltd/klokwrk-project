package net.croz.cargotracker.api.open.shared.conversation

interface OperationMessage<P, M extends Map<String, ?>> {
  P getPayload()
  M getMetaData()
}
