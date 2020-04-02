package net.croz.cargotracker.shared.operation

interface OperationMessage<P, M extends Map<String, ?>> {
  P getPayload()
  M getMetaData()
}
