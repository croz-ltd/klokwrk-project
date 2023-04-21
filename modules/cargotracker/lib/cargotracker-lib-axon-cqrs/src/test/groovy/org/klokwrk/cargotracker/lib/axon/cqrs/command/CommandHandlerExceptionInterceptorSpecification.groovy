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
package org.klokwrk.cargotracker.lib.axon.cqrs.command

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
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
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class CommandHandlerExceptionInterceptorSpecification extends Specification {
  UnitOfWork<Message<?>> unitOfWork
  InterceptorChain interceptorChainMock

  @SuppressWarnings("CodeNarc.EmptyClass")
  static class StubCommand {
  }

  void setup() {
    unitOfWork = new DefaultUnitOfWork<>(new GenericMessage<Object>(new StubCommand()))
    interceptorChainMock = Stub()
  }

  private List configureLoggerAndListAppender() {
    Logger logger = LoggerFactory.getLogger("org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor") as Logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
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
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
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
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        message == "Execution of 'StubCommand' command handler failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)

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
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO
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
      listAppender.list.size() == 0
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    domainExceptionParam                            | _
    new DomainException(ViolationInfo.BAD_REQUEST)  | _
    new CommandException(ViolationInfo.BAD_REQUEST) | _
  }

  void "when QueryException is thrown should handle it like domain exception and log a warning message"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO
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
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.WARN
        message.startsWith("QueryException is thrown during command handling, which is unexpected.")
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not log QueryException occurrence on a level higher than warning"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.ERROR
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
      listAppender.list.size() == 0
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "when unexpected error is thrown, should catch, wrap and log on error level"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
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
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.ERROR
        message.startsWith("Execution of command handler failed [detailsException.exceptionId:")
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    causeExceptionMessageParam | remoteHandlerExceptionMessageParam
    "Some illegal arguments"   | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    null                       | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
    ""                         | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
    "   "                      | "Execution of 'StubCommand' command failed because of java.lang.IllegalArgumentException"
  }
}
