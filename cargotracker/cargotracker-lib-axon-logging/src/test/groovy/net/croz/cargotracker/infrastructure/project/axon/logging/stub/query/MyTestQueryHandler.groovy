package net.croz.cargotracker.infrastructure.project.axon.logging.stub.query

import org.axonframework.queryhandling.QueryHandler

class MyTestQueryHandler {
  @SuppressWarnings(["unused", "UnusedMethodParameter"])
  @QueryHandler
  Map handleSomeQuery(MyTestQuery myTestQuery) {
    return [testKey: "testValue"]
  }
}
