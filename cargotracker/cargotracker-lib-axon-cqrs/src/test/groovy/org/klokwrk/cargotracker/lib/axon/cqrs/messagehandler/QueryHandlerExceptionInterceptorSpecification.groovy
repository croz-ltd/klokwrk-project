package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import com.google.common.collect.ImmutableList
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
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
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> "ok"

    when:
    Object result = queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    result == "ok"
  }

  void "should catch and handle QueryException thrown from the handler"() {
    given:
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new QueryException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    verifyAll(queryExecutionException.details.get(), QueryException, { QueryException queryException ->
      queryException.violationInfo == ViolationInfo.BAD_REQUEST
    })
  }

  void "should catch QueryException thrown from the handler and log it at the debug level"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw queryExceptionParam }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Query execution failed for business reasons (normal execution flow): ${queryExceptionMessageParam}"
    verifyAll(queryExecutionException.details.get(), QueryException, { QueryException queryException ->
      queryException.violationInfo == ViolationInfo.BAD_REQUEST
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.DEBUG
      loggingEvents[0].message == "Execution of query handler failed for business reasons (normal execution flow): ${queryExceptionMessageParam}"
    }

    cleanup:
    TestLoggerFactory.clearAll()

    where:
    queryExceptionMessageParam                          | queryExceptionParam
    ViolationInfo.BAD_REQUEST.violationCode.codeMessage | new QueryException(ViolationInfo.BAD_REQUEST)
    "Some query exception message"                      | new QueryException(ViolationInfo.BAD_REQUEST, queryExceptionMessageParam)
  }

  void "should catch QueryException thrown from the handler and should not log it at the level higher than debug"() {
    given:
    TestLoggerFactory.clearAll()
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new QueryException(ViolationInfo.BAD_REQUEST) }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Query execution failed for business reasons (normal execution flow): Bad Request"
    verifyAll(queryExecutionException.details.get(), QueryException, { QueryException queryException ->
      queryException.violationInfo == ViolationInfo.BAD_REQUEST
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
    TestLogger logger = TestLoggerFactory.getTestLogger("org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler.QueryHandlerExceptionInterceptor")
    logger.setEnabledLevels(Level.ERROR, Level.WARN, Level.INFO)

    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = new QueryHandlerExceptionInterceptor()
    interceptorChainMock.proceed() >> { throw new IllegalArgumentException("Some illegal arguments") }

    when:
    queryHandlerExceptionInterceptor.handle(unitOfWork, interceptorChainMock)

    then:
    QueryExecutionException queryExecutionException = thrown()

    queryExecutionException.details.present
    queryExecutionException.message == "Query execution failed."
    queryExecutionException.cause instanceof IllegalArgumentException
    queryExecutionException.cause.message == "Some illegal arguments"

    queryExecutionException.details.present
    verifyAll(queryExecutionException.details.get(), DomainException, { DomainException domainException ->
      domainException.violationInfo == ViolationInfo.UNKNOWN
    })

    new PollingConditions(timeout: 5, initialDelay: 0.5, delay: 0.5).eventually {
      ImmutableList<LoggingEvent> loggingEvents = logger.allLoggingEvents
      loggingEvents.size() == 1
      loggingEvents[0].level == Level.ERROR
      loggingEvents[0].message == "Execution of query handler failed."
    }

    cleanup:
    TestLoggerFactory.clearAll()
  }
}
