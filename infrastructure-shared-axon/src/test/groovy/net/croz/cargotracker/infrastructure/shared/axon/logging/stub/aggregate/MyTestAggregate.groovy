package net.croz.cargotracker.infrastructure.shared.axon.logging.stub.aggregate

import net.croz.cargotracker.infrastructure.shared.axon.logging.stub.command.CreateMyTestAggregateCommand
import net.croz.cargotracker.infrastructure.shared.axon.logging.stub.command.UpdateMyTestAggregateCommand
import net.croz.cargotracker.infrastructure.shared.axon.logging.stub.event.MyTestAggregateCreatedEvent
import net.croz.cargotracker.infrastructure.shared.axon.logging.stub.event.MyTestAggregateUpdatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateRoot

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
  void update(UpdateMyTestAggregateCommand cmd) {
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
