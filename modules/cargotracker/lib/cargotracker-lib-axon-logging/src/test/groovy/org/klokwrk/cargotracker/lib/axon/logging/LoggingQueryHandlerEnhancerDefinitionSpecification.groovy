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

import com.google.common.collect.ImmutableList
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition
import org.axonframework.messaging.annotation.ClasspathHandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MultiHandlerDefinition
import org.axonframework.messaging.annotation.MultiHandlerEnhancerDefinition
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.lib.axon.logging.stub.query.MyTestQuery
import org.klokwrk.cargotracker.lib.axon.logging.stub.query.MyTestQueryHandler
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class LoggingQueryHandlerEnhancerDefinitionSpecification extends Specification {
  Configuration axonConfiguration
  QueryGateway axonQueryGateway

  void setup() {
    TestLoggerFactory.clearAll()
//    TestLoggerFactory.instance.printLevel = Level.DEBUG // uncomment if you want to see logging output during the test

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
    TestLoggerFactory.clearAll()

    axonConfiguration.shutdown()
    axonQueryGateway = null
    axonConfiguration = null
  }

  void "should work for query handler"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.query-handler-logging")

    when:
    axonQueryGateway.query(new MyTestQuery(query: "123"), Map).join()

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message ==~ /Executing QueryHandler method \[MyTestQueryHandler.handleSomeQuery\(MyTestQuery\)] with payload \[query:123]/
    }
  }

  void "should not log for logger level higher than DEBUG"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.query-handler-logging")
    logger.enabledLevelsForAllThreads = Level.INFO

    when:
    axonQueryGateway.query(new MyTestQuery(query: "123"), Map).join()

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }
  }
}
