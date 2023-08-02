/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition
import org.axonframework.messaging.annotation.ClasspathHandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MultiHandlerDefinition
import org.axonframework.messaging.annotation.MultiHandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.stub.aggregate.MyTestAggregate
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.CreateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.CreateMyTestAggregateWithoutExpectedIdentifierCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.UpdateMyTestAggregateCommand
import org.klokwrk.cargotracker.lib.axon.logging.stub.command.UpdateMyTestAggregateWithoutExpectedIdentifiersCommand
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class LoggingCommandHandlerEnhancerDefinitionSpecification extends Specification {
  Configuration axonConfiguration
  CommandGateway axonCommandGateway

  void setup() {
    Configurer axonConfigurer = DefaultConfigurer.defaultConfiguration()
    axonConfigurer.configureEmbeddedEventStore((Configuration axonConfiguration) -> new InMemoryEventStorageEngine())
                  .configureAggregate(MyTestAggregate)
                  .registerHandlerDefinition((Configuration configuration, Class inspectedClass) -> {
                    MultiHandlerDefinition multiHandlerDefinition = MultiHandlerDefinition.ordered(
                        MultiHandlerEnhancerDefinition.ordered(ClasspathHandlerEnhancerDefinition.forClass(inspectedClass), new LoggingCommandHandlerEnhancerDefinition()),
                        ClasspathHandlerDefinition.forClass(inspectedClass)
                    )

                    return multiHandlerDefinition
                  })

    axonConfiguration = axonConfigurer.buildConfiguration()
    axonConfiguration.start()

    axonCommandGateway = axonConfiguration.commandGateway()
  }

  void cleanup() {
    axonConfiguration.shutdown()
    axonCommandGateway = null
    axonConfiguration = null
  }

  private List configureLoggerAndListAppender() {
    Logger logger = LoggerFactory.getLogger("cargotracker.axon.command-handler-logging") as Logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
  }

  void "should work for constructor command handler"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    String aggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        message == "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateCommand)] with command [CreateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier)]"
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should work for constructor command handler when command does not have expected aggregate identifier"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    String unexpectedAggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateWithoutExpectedIdentifierCommand(unexpectedAggregateIdentifier: unexpectedAggregateIdentifier, name: "bla"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        String expectedMessage = "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateWithoutExpectedIdentifierCommand)] with command " +
                                 "[CreateMyTestAggregateWithoutExpectedIdentifierCommand(aggregateIdentifier: ${ CommonConstants.NOT_AVAILABLE })]"
        message == expectedMessage
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should work for method command handler"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    String aggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 2
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        message == "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateCommand)] with command [CreateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier)]"
      }
      verifyAll(listAppender.list[1]) {
        level == Level.DEBUG
        message == "Executing CommandHandler method [MyTestAggregate.update(UpdateMyTestAggregateCommand)] with command [UpdateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier, sequenceNumber: 0)]"
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should work for method command handler when command does not have expected identifiers"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    String unexpectedAggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateWithoutExpectedIdentifierCommand(unexpectedAggregateIdentifier: unexpectedAggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateWithoutExpectedIdentifiersCommand(unexpectedAggregateIdentifier: unexpectedAggregateIdentifier, unexpectedSequenceNumber: 0, name: "ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 2
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        String expectedMessage = "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateWithoutExpectedIdentifierCommand)] with command " +
                                 "[CreateMyTestAggregateWithoutExpectedIdentifierCommand(aggregateIdentifier: ${ CommonConstants.NOT_AVAILABLE })]"
        message == expectedMessage
      }
      verifyAll(listAppender.list[1]) {
        level == Level.DEBUG
        String expectedMessage = "Executing CommandHandler method [MyTestAggregate.update(UpdateMyTestAggregateWithoutExpectedIdentifiersCommand)] with command " +
                                 "[UpdateMyTestAggregateWithoutExpectedIdentifiersCommand(aggregateIdentifier: ${ CommonConstants.NOT_AVAILABLE }, sequenceNumber: ${ CommonConstants.NOT_AVAILABLE })]"
        message == expectedMessage
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not log for logger level higher than DEBUG"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO
    String aggregateIdentifier = UUID.randomUUID()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 0
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }
}
