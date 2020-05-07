package org.klokwrk.cargotracker.lib.axon.logging.stub.projection

import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateCreatedEvent
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateUpdatedEvent

class MyTestProjector {
  @SuppressWarnings(["unused", "UnusedMethodParameter"])
  @EventHandler
  void handle(MyTestAggregateCreatedEvent event) {
    // do nothing
  }

  @SuppressWarnings(["unused", "UnusedMethodParameter"])
  @EventHandler
  void handle(MyTestAggregateUpdatedEvent event) {
    // do nothing
  }
}
