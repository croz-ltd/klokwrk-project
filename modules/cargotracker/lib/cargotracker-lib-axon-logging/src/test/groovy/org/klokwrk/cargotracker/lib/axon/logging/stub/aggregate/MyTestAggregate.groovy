/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
