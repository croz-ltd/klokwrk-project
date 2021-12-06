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
package org.klokwrk.cargotracker.lib.axon.cqrs.query

import com.google.common.collect.ImmutableList
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
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

class QueryHandlerExceptionInterceptorSpecification extends Specification {
  UnitOfWork<Message<?>> unitOfWork
  InterceptorChain interceptorChainMock

  @SuppressWarnings("CodeNarc.EmptyClass")
  static class StubQuery {
  }

  void setup() {
    unitOfWork = new DefaultUnitOfWork<>(new GenericMessage<Object>(new StubQuery()))
    interceptorChainMock = Stub()

    // uncomment if you want to see logging output during the test
//    TestLoggerFactory.instance.printLevel = Level.DEBUG
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
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Execution of 'StubQuery' query handler failed for business reasons (normal execution flow): ${ domainExceptionMessageParam }"
    }

    cleanup:
    TestLoggerFactory.clearAll()

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
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw domainExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    domainExceptionParam                           | _
    new DomainException(ViolationInfo.BAD_REQUEST) | _
    new QueryException(ViolationInfo.BAD_REQUEST)  | _
  }

  void "when CommandException is thrown should handle it like domain exception and log a warning message"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.WARN
      loggingEvents[0].message.startsWith("CommandException is thrown during query handling, which is unexpected.")
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }

  @SuppressWarnings("CodeNarc.UnnecessarySetter")
  void "should not log CommandException occurrence on a level higher than warning"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new CommandException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Execution of 'StubQuery' query failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), CommandException, { CommandException commandException ->
      commandException.violationInfo == ViolationInfo.BAD_REQUEST
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
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException(causeExceptionMessageParam as String) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.cause instanceof IllegalArgumentException
    queryExecutionException.cause.message == causeExceptionMessageParam

    queryExecutionException.details.present
    verifyAll(queryExecutionException.details.get(), RemoteHandlerException, { RemoteHandlerException remoteHandlerException ->
      queryExecutionException.message == "Execution of 'StubQuery' query failed [detailsException.exceptionId: ${ remoteHandlerException.exceptionId }]"
      remoteHandlerException.message == remoteHandlerExceptionMessageParam
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.ERROR
      loggingEvents[0].message.startsWith("Execution of query handler failed [detailsException.exceptionId:")
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    causeExceptionMessageParam | remoteHandlerExceptionMessageParam
    "Some illegal arguments"   | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    null                       | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
    ""                         | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
    "   "                      | "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException"
  }

  void "when unexpected error is thrown and error logging is not enabled, should catch and wrap, but should not log anything"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.WARN, Level.INFO, Level.DEBUG)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException("Some illegal arguments") }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.cause instanceof IllegalArgumentException
    queryExecutionException.cause.message == "Some illegal arguments"

    queryExecutionException.details.present
    verifyAll(queryExecutionException.details.get(), RemoteHandlerException, { RemoteHandlerException remoteHandlerException ->
      queryExecutionException.message == "Execution of 'StubQuery' query failed [detailsException.exceptionId: ${ remoteHandlerException.exceptionId }]"
      remoteHandlerException.message == "Execution of 'StubQuery' query failed because of java.lang.IllegalArgumentException: Some illegal arguments"
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 0
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }
}
