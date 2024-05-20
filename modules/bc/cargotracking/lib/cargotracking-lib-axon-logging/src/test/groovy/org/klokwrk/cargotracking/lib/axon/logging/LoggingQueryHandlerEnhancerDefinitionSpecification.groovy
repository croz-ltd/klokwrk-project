/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.lib.axon.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition
import org.axonframework.messaging.annotation.ClasspathHandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MultiHandlerDefinition
import org.axonframework.messaging.annotation.MultiHandlerEnhancerDefinition
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracking.lib.axon.logging.stub.query.MyTestQuery
import org.klokwrk.cargotracking.lib.axon.logging.stub.query.MyTestQueryHandler
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class LoggingQueryHandlerEnhancerDefinitionSpecification extends Specification {
  Configuration axonConfiguration
  QueryGateway axonQueryGateway

  void setup() {
    Configurer axonConfigurer = DefaultConfigurer.defaultConfiguration()
    axonConfigurer.configureEmbeddedEventStore((Configuration axonConfiguration) -> new InMemoryEventStorageEngine())
                  .registerQueryHandler((Configuration configuration) -> new MyTestQueryHandler())
                  .registerHandlerDefinition((Configuration configuration, Class inspectedClass) -> {
                    MultiHandlerDefinition multiHandlerDefinition = MultiHandlerDefinition.ordered(
                        MultiHandlerEnhancerDefinition.ordered(ClasspathHandlerEnhancerDefinition.forClass(inspectedClass), new LoggingQueryHandlerEnhancerDefinition()),
                        ClasspathHandlerDefinition.forClass(inspectedClass)
                    )

                    return multiHandlerDefinition
                  })

    axonConfiguration = axonConfigurer.buildConfiguration()
    axonConfiguration.start()

    axonQueryGateway = axonConfiguration.queryGateway()
  }

  void cleanup() {
    axonConfiguration.shutdown()
    axonQueryGateway = null
    axonConfiguration = null
  }

  private List configureLoggerAndListAppender() {
    Logger logger = LoggerFactory.getLogger("cargotracking-lib-axon-logging.query-handler-logger") as Logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
  }

  void "should work for query handler"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()

    when:
    axonQueryGateway.query(new MyTestQuery(query: "123"), Map).join()

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        message ==~ /Executing QueryHandler method \[MyTestQueryHandler.handleSomeQuery\(MyTestQuery\)] with payload \[query:123]/
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not log for logger level higher than DEBUG"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO

    when:
    axonQueryGateway.query(new MyTestQuery(query: "123"), Map).join()

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 0
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }
}
