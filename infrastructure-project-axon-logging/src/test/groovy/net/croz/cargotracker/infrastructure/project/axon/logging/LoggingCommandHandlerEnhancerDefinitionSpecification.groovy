package net.croz.cargotracker.infrastructure.project.axon.logging

import com.google.common.collect.ImmutableList
import net.croz.cargotracker.infrastructure.project.axon.logging.stub.aggregate.MyTestAggregate
import net.croz.cargotracker.infrastructure.project.axon.logging.stub.command.CreateMyTestAggregateCommand
import net.croz.cargotracker.infrastructure.project.axon.logging.stub.command.UpdateMyTestAggregateCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition
import org.axonframework.messaging.annotation.ClasspathHandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MultiHandlerDefinition
import org.axonframework.messaging.annotation.MultiHandlerEnhancerDefinition
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class LoggingCommandHandlerEnhancerDefinitionSpecification extends Specification {
  Configuration axonConfiguration
  CommandGateway axonCommandGateway

  @SuppressWarnings("Indentation")
  void setup() {
    TestLoggerFactory.clearAll()
//    TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG) // uncomment if you want to see logging output during the test

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

  @SuppressWarnings("unused")
  void cleanup() {
    TestLoggerFactory.clearAll()

    axonConfiguration.shutdown()
    axonCommandGateway = null
    axonConfiguration = null
  }

  void "should work for constructor command handler"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.command-handler-logging")
    String aggregateIdentifier = UUID.randomUUID().toString()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents

      loggingEvents.size() == 1

      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateCommand)] with command [CreateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier)]"
    }
  }

  void "should work for method command handler"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.command-handler-logging")
    String aggregateIdentifier = UUID.randomUUID().toString()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents

      loggingEvents.size() == 2

      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Executing CommandHandler constructor [MyTestAggregate(CreateMyTestAggregateCommand)] with command [CreateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier)]"

      loggingEvents[1].level == Level.DEBUG
      loggingEvents[1].message == "Executing CommandHandler method [MyTestAggregate.update(UpdateMyTestAggregateCommand)] with command [UpdateMyTestAggregateCommand(aggregateIdentifier: $aggregateIdentifier, sequenceNumber: 0)]"
    }
  }

  void "should not log for logger level higher than DEBUG"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger("cargotracker.axon.command-handler-logging")
    logger.enabledLevelsForAllThreads = Level.INFO
    String aggregateIdentifier = UUID.randomUUID().toString()

    when:
    axonCommandGateway.sendAndWait(new CreateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, name: "bla"))
    axonCommandGateway.sendAndWait(new UpdateMyTestAggregateCommand(aggregateIdentifier: aggregateIdentifier, sequenceNumber: 0, name: "ble"))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }
  }
}
