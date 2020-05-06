package net.croz.cargotracker.infrastructure.project.axon.logging.stub.projection

import net.croz.cargotracker.infrastructure.project.axon.logging.stub.event.MyTestAggregateCreatedEvent
import net.croz.cargotracker.infrastructure.project.axon.logging.stub.event.MyTestAggregateUpdatedEvent
import org.axonframework.eventhandling.EventHandler

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
