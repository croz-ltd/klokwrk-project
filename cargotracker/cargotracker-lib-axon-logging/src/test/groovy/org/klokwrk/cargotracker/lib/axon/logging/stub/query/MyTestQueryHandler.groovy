package org.klokwrk.cargotracker.lib.axon.logging.stub.query

import org.axonframework.queryhandling.QueryHandler

class MyTestQueryHandler {
  @SuppressWarnings(["unused", "UnusedMethodParameter"])
  @QueryHandler
  Map handleSomeQuery(MyTestQuery myTestQuery) {
    return [testKey: "testValue"]
  }
}
