package net.croz.cargotracker.infrastructure.shared.axon.logging.stub.query

import org.axonframework.queryhandling.QueryHandler

class MyTestQueryHandler {
  @SuppressWarnings("unused")
  @QueryHandler
  Map handleSomeQuery(MyTestQuery myTestQuery) {
    return [testKey: "testValue"]
  }
}
