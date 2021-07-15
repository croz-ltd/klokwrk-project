package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import com.google.common.collect.ImmutableList
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class CommandHandlerExceptionInterceptorSpecification extends Specification {
  UnitOfWork<Message<?>> unitOfWork
  InterceptorChain interceptorChainMock

  @SuppressWarnings("CodeNarc.EmptyClass")
  static class StubMessage {
  }

  void setup() {
    unitOfWork = new DefaultUnitOfWork<>(new GenericMessage<Object>(new StubMessage()))
    interceptorChainMock = Stub()

    // uncomment if you want to see logging output during the test
//    TestLoggerFactory.instance.printLevel = Level.DEBUG
  }

  void "should work for non-exceptional scenario"() {
    given:
    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> "ok"

    when:
    Object result = commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    result == "ok"
  }

  void "should catch and handle CommandException thrown from the handler"() {
    given:
    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.details.present
    verifyAll(commandExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
    })
  }

  void "should catch CommandException thrown from the handler and log it at the debug level"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw commandExceptionParam }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.details.present
    commandExecutionException.message == "Command execution failed for business reasons (normal execution flow): ${commandExceptionMessageParam}"
    verifyAll(commandExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Execution of command handler failed for business reasons (normal execution flow): ${commandExceptionMessageParam}"
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    commandExceptionMessageParam                        | commandExceptionParam
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new CommandException(ViolationInfo.BAD_REQUEST)
    "Some command exception message"                    | new CommandException(ViolationInfo.BAD_REQUEST, commandExceptionMessageParam)
  }

  void "should catch CommandException thrown from the handler and should not log it at the level higher than debug"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.details.present
    commandExecutionException.message == "Command execution failed for business reasons (normal execution flow): Bad Request"
    verifyAll(commandExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }

  void "should catch and handle any other thrown from the handler"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException("Some illegal arguments") }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.details.present
    commandExecutionException.message == "Command execution failed."
    commandExecutionException.cause instanceof IllegalArgumentException
    commandExecutionException.cause.message == "Some illegal arguments"

    commandExecutionException.details.present
    verifyAll(commandExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.UNKNOWN
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.ERROR
      loggingEvents[0].message == "Execution of command handler failed."
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }
}
