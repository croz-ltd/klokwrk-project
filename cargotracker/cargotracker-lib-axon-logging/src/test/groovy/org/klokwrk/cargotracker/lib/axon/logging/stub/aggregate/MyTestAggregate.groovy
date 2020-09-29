package org.klokwrk.cargotracker.lib.axon.logging.stub.aggregate

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateRoot
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.CreateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.CreateMyTestAggregateWithoutExpectedIdentifierCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.UpdateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.UpdateMyTestAggregateWithoutExpectedIdentifiersCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateCreatedEvent
import org.klokwrk.cargotracker.lib.axon.logging.stub.event.MyTestAggregateUpdatedEvent

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@AggregateRoot
class MyTestAggregate {
  @AggregateIdentifier
  String aggregateIdentifier

  String name

  @SuppressWarnings("unused")
  MyTestAggregate() {
  }

  @CommandHandler
  MyTestAggregate(CreateMyTestAggregateCommand cmd) {
    apply(new MyTestAggregateCreatedEvent(aggregateIdentifier: cmd.aggregateIdentifier, name: cmd.name))
  }

  @CommandHandler
  MyTestAggregate(CreateMyTestAggregateWithoutExpectedIdentifierCommand cmd) {
    apply(new MyTestAggregateCreatedEvent(aggregateIdentifier: cmd.unexpectedAggregateIdentifier, name: cmd.name))
  }

  @CommandHandler
  void update(UpdateMyTestAggregateCommand cmd) {
    apply(new MyTestAggregateUpdatedEvent(name: cmd.name))
  }

  @CommandHandler
  void update(UpdateMyTestAggregateWithoutExpectedIdentifiersCommand cmd) {
    apply(new MyTestAggregateUpdatedEvent(name: cmd.name))
  }

  @EventSourcingHandler
  void onMyTestAggregateCreatedEvent(MyTestAggregateCreatedEvent myTestAggregateCreatedEvent) {
    this.aggregateIdentifier = myTestAggregateCreatedEvent.aggregateIdentifier
    this.name = myTestAggregateCreatedEvent.name
  }

  @EventSourcingHandler
  void onMyTestAggregateUpdatedEvent(MyTestAggregateUpdatedEvent myTestAggregateUpdatedEvent) {
    this.name = myTestAggregateUpdatedEvent.name
  }
}
