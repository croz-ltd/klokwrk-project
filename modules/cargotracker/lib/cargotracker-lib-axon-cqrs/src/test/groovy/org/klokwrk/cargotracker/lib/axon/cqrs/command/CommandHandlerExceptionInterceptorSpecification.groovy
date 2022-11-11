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
package org.klokwrk.cargotracker.lib.axon.cqrs.command

import com.google.common.collect.ImmutableList
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.klokwrk.cargotracker.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
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
  static class StubCommand {
  }

  void setup() {
    unitOfWork = new DefaultUnitOfWork<>(new GenericMessage<Object>(new StubCommand()))
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

  void "should catch and handle Domain and Command exceptions thrown from the handler"() {
    given:
    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    verifyAll(commandExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    where:
    domainExceptionParam                            | _
    new DomainException(ViolationInfo.BAD_REQUEST)  | _
    new CommandException(ViolationInfo.BAD_REQUEST) | _
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should catch Domain and Command thrown from the handler and log them at the debug level"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.message == "Execution of 'StubCommand' command failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
    verifyAll(commandExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Execution of 'StubCommand' command handler failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    domainExceptionMessageParam                         | domainExceptionParam
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST)
    "Some domain¬ exception message"                    | new DomainException(ViolationInfo.BAD_REQUEST, domainExceptionMessageParam)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, null)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, "")
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, "   ")

    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new CommandException(ViolationInfo.BAD_REQUEST)
    "Some command exception message"                    | new CommandException(ViolationInfo.BAD_REQUEST, domainExceptionMessageParam)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new CommandException(ViolationInfo.BAD_REQUEST, null)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new CommandException(ViolationInfo.BAD_REQUEST, "")
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new CommandException(ViolationInfo.BAD_REQUEST, "   ")
  }

  void "should catch Domain and Command thrown from the handler and should not log them at the level higher than debug"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.message == "Execution of 'StubCommand' command failed for business reasons (normal execution flow): Bad Request"
    verifyAll(commandExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    domainExceptionParam                            | _
    new DomainException(ViolationInfo.BAD_REQUEST)  | _
    new CommandException(ViolationInfo.BAD_REQUEST) | _
  }

  void "when QueryException is thrown should handle it like domain exception and log a warning message"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new QueryException(ViolationInfo.BAD_REQUEST) }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.message == "Execution of 'StubCommand' command failed for business reasons (normal execution flow): Bad Request"
    verifyAll(commandExecutionException.details.get(), QueryException, { QueryException queryException ->
      queryException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.WARN
      loggingEvents[0].message.startsWith("QueryException is thrown during command handling, which is unexpected.")
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }

  @SuppressWarnings("CodeNarc.UnnecessarySetter")
  void "should not log QueryException occurrence on a level higher than warning"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new QueryException(ViolationInfo.BAD_REQUEST) }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.message == "Execution of 'StubCommand' command failed for business reasons (normal execution flow): Bad Request"
    verifyAll(commandExecutionException.details.get(), QueryException, { QueryException queryException ->
      queryException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }

  void "when unexpected error is thrown, should catch, wrap and log on error level"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException(causeExceptionMessageParam as String) }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.cause instanceof IllegalArgumentException
    commandExecutionException.cause.message == causeExceptionMessageParam

    commandExecutionException.details.present
    verifyAll(commandExecutionException.details.get(), RemoteHandlerException, { RemoteHandlerException remoteHandlerException ->
      commandExecutionException.message == "Execution of 'StubCommand' command failed [detailsException.exceptionId: ${ remoteHandlerException.exceptionId }]"
      remoteHandlerException.message == remoteHandlerExceptionMessageParam
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.ERROR
      loggingEvents[0].message.startsWith("Execution of command handler failed [detailsException.exceptionId:")
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    causeExceptionMessageParam | remoteHandlerExceptionMessageParam
    "Some illegal arguments"   | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    null                       | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
    ""                         | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
    "   "                      | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
  }

  void "when unexpected error is thrown and error logging is not enabled, should catch and wrap, but should not log anything"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.WARN, Level.INFO, Level.DEBUG)

    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = new CommandHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException("Some illegal arguments") }

    when:
    commandHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    CommandExecutionException commandExecutionException = thrown()

    commandExecutionException.stackTrace.size() == 0
    commandExecutionException.details.present
    commandExecutionException.cause instanceof IllegalArgumentException
    commandExecutionException.cause.message == "Some illegal arguments"

    commandExecutionException.details.present
    verifyAll(commandExecutionException.details.get(), RemoteHandlerException, { RemoteHandlerException remoteHandlerException ->
      commandExecutionException.message == "Execution of 'StubCommand' command failed [detailsException.exceptionId: ${ remoteHandlerException.exceptionId }]"
      remoteHandlerException.message == "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }
}
