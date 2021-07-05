/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.lib.axon.logging

import com.google.common.collect.ImmutableList
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.config.EventProcessingModule
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition
import org.axonframework.messaging.annotation.ClasspathHandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MultiHandlerDefinition
import org.axonframework.messaging.annotation.MultiHandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.stub.aggregate.MyTestAggregate
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.CreateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.UpdateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.projection.MyTestProjector
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class LoggingEventSourcingHandlerEnhancerDefinitionSpecification extends Specification {
  Configuration axonConfiguration
  CommandGateway axonCommandGateway

  void setup() {
    TestLoggerFactory.clearAll()
//    TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG) // uncomment if you want to see logging output during the test

    // Although not needed, here we register event processing to to validate distinction between EventSourcing and Event messages.
    EventProcessingModule eventProcessingModule = new EventProcessingModule()
    eventProcessingModule.registerEventHandler((Configuration axonConfiguration) -> new MyTestProjector())

    Configurer axonConfigurer = DefaultConfigurer.defaultConfiguration()
    axonConfigurer.configureEmbeddedEventStore((Configuration axonConfiguration) -> new InMemoryEventStorageEngine())
                  .configureAggregate(MyTestAggregate)
                  .registerModule(eventProcessingModule)
                  .registerHandlerDefinition((Configuration configuration, Class inspectedClass) -> {
                    MultiHandlerDefinition multiHandlerDefinition = MultiHandlerDefinition.ordered(
                        MultiHandlerEnhancerDefinition.ordered(ClasspathHandlerEnhancerDefinition.forClass(inspectedClass), new LoggingEventSourcingHandlerEnhancerDefinition()),
                        ClasspathHandlerDefinition.forClass(inspectedClass)
                    )

                    return multiHandlerDefinition
                  })

    axonConfiguration = axonConfigurer.buildConfiguration()
    axonConfiguration.start()

    axonCommandGateway = axonConfiguration.commandGateway()
  }

  void cleanup() {
    TestLoggerFactory.clearAll()

    axonConfiguration.shutdown()
    axonCommandGateway = null
    axonConfiguration = null
  }

  void "should work for event sourcing handler"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.eventsourcing-handler-logging")
    String aggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 1, name: "bla ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 6
      loggingEvents*.level == (0..5).collect({ Level.DEBUG })

      // sample message: Executing EventSourcingHandler method [MyTestAggregate.onMyTestAggregateCreatedEvent(MyTestAggregateCreatedEvent)] with event [eventId: 38a0c0fa-0b79-456a-b9cc-a285f97e1e09, MyTestAggregateCreatedEvent(aggregateIdentifier: ae587429-a07b-4e4c-a251-25691a38944e, sequenceNumber: 0)]
      3 == loggingEvents.findAll({
        Boolean containsAggregateIdentifier = it.message.contains(aggregateIdentifier)
        Boolean matchesRegex = it.message ==~ /Executing EventSourcingHandler method \[MyTestAggregate.onMyTestAggregateCreatedEvent\(MyTestAggregateCreatedEvent\)] with event \[eventId: \p{Graph}{36}, MyTestAggregateCreatedEvent\(aggregateIdentifier: \p{Graph}{36}, sequenceNumber: 0\)]/
        return containsAggregateIdentifier && matchesRegex
      }).size()

      // sample message: Executing EventSourcingHandler method [MyTestAggregate.onMyTestAggregateUpdatedEvent(MyTestAggregateUpdatedEvent)] with event [eventId: 91fb0e76-1e85-488a-84a4-d51bfdc548c7, MyTestAggregateUpdatedEvent(aggregateIdentifier: ae587429-a07b-4e4c-a251-25691a38944e, sequenceNumber: 1)]
      2 == loggingEvents.findAll({
        Boolean containsAggregateIdentifier = it.message.contains(aggregateIdentifier)
        Boolean matchesRegex = it.message ==~ /Executing EventSourcingHandler method \[MyTestAggregate.onMyTestAggregateUpdatedEvent\(MyTestAggregateUpdatedEvent\)] with event \[eventId: \p{Graph}{36}, MyTestAggregateUpdatedEvent\(aggregateIdentifier: \p{Graph}{36}, sequenceNumber: 1\)]/
        return containsAggregateIdentifier && matchesRegex
      }).size()

      // sample message: Executing EventSourcingHandler method [MyTestAggregate.onMyTestAggregateUpdatedEvent(MyTestAggregateUpdatedEvent)] with event [eventId: e034db67-8d7c-496e-be6f-9ad7b07f271a, MyTestAggregateUpdatedEvent(aggregateIdentifier: ae587429-a07b-4e4c-a251-25691a38944e, sequenceNumber: 2)]
      1 == loggingEvents.findAll({
        Boolean containsAggregateIdentifier = it.message.contains(aggregateIdentifier)
        Boolean matchesRegex = it.message ==~ /Executing EventSourcingHandler method \[MyTestAggregate.onMyTestAggregateUpdatedEvent\(MyTestAggregateUpdatedEvent\)] with event \[eventId: \p{Graph}{36}, MyTestAggregateUpdatedEvent\(aggregateIdentifier: \p{Graph}{36}, sequenceNumber: 2\)]/
        return containsAggregateIdentifier && matchesRegex
      }).size()
    }
  }

  void "should not log for logger level higher than DEBUG"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.eventsourcing-handler-logging")
    logger.enabledLevelsForAllThreads = Level.INFO
    String aggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 1, name: "bla ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }
  }
}
