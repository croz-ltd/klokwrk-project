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
package org.klokwrk.cargotracking.lib.axon.cqrs.query

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracking.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class QueryHandlerExceptionInterceptorSpecification extends Specification {
  UnitOfWork<Message<?>> unitOfWork
  InterceptorChain interceptorChainMock

  @SuppressWarnings("CodeNarc.EmptyClass")
  static class StubQuery {
  }

  void setup() {
    unitOfWork = new DefaultUnitOfWork<>(new GenericMessage<Object>(new StubQuery()))
    interceptorChainMock = Stub()
  }

  private List configureLoggerAndListAppender() {
    Logger logger = LoggerFactory.getLogger("org.klokwrk.cargotracking.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor") as Logger
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
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> "ok"

    when:
    Object result = queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    result == "ok"
  }

  void "should catch and handle Domain and Query exceptions thrown from the handler"() {
    given:
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    where:
    domainExceptionParam                           | _
    new DomainException(ViolationInfo.BAD_REQUEST) | _
    new QueryException(ViolationInfo.BAD_REQUEST)  | _
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should catch Domain and Query exceptions thrown from the handler and log them at the debug level"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.DEBUG
        message == "Execution of 'StubQuery' query handler failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    domainExceptionMessageParam                         | domainExceptionParam
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST)
    "Some query exception message"                      | new DomainException(ViolationInfo.BAD_REQUEST, domainExceptionMessageParam)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, null)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, "")
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new DomainException(ViolationInfo.BAD_REQUEST, "   ")

    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new QueryException(ViolationInfo.BAD_REQUEST)
    "Some query exception message"                      | new QueryException(ViolationInfo.BAD_REQUEST, domainExceptionMessageParam)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new QueryException(ViolationInfo.BAD_REQUEST, null)
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new QueryException(ViolationInfo.BAD_REQUEST, "")
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new QueryException(ViolationInfo.BAD_REQUEST, "   ")
  }

  void "should catch Domain and Query exceptions thrown from the handler and should not log them at the level higher than debug"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 0
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    domainExceptionParam                           | _
    new DomainException(ViolationInfo.BAD_REQUEST) | _
    new QueryException(ViolationInfo.BAD_REQUEST)  | _
  }

  void "when CommandException is thrown should handle it like domain exception and log a warning message"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.INFO
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.WARN
        message.startsWith("CommandException is thrown during query handling, which is unexpected.")
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }

  void "should not log CommandException occurrence on a level higher than warning"() {
    given:
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()
    logger.level = Level.ERROR

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
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
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException(causeExceptionMessageParam as String) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.stackTrace.size() == 0
    queryExecutionException.details.present
    queryExecutionException.cause instanceof IllegalArgumentException
    queryExecutionException.cause.message == causeExceptionMessageParam

    verifyAll(queryExecutionException.details.get(), RemoteHandlerException, { RemoteHandlerException remoteHandlerException ->
      queryExecutionException.message == "Execution of 'StubQuery' query failed [detailsException.exceptionId: ${ remoteHandlerException.exceptionId }]"
      remoteHandlerException.message == remoteHandlerExceptionMessageParam
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      listAppender.list.size() == 1
      verifyAll(listAppender.list[0]) {
        level == Level.ERROR
        message.startsWith("Execution of query handler failed [detailsException.exceptionId:")
      }
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    causeExceptionMessageParam | remoteHandlerExceptionMessageParam
    "Some illegal arguments"   | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    null                       | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
    ""                         | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
    "   "                      | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
  }
}
