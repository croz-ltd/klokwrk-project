package org.klokwrk.cargotracker.lib.axon.cqrs.query

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.RemoteHandlerException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException

/**
 * Simplifies throwing a business exception from query handling code, making sure it is propagated back to the caller as a details field of Axon's {@code QueryExecutionException}.
 * <p/>
 * It logs the stacktrace of anticipated {@code QueryExecutionException} at the debug level, which helps during development.
 * <p/>
 * In case of unexpected exceptions, corresponding {@code QueryExecutionException} is logged at the error level, and exception details are represented with {@code RemoteHandlerException} instance.
 * {@code exceptionId} property of {@code RemoteHandlerException} instance can be used in other JVM for correlation via logging.
 */
@Slf4j
@CompileStatic
class QueryHandlerExceptionInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {
  @SuppressWarnings("CodeNarc.CatchException")
  @Override
  Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain) throws Exception {
    try {
      Object returnValue = interceptorChain.proceed()
      return returnValue
    }
    catch (QueryException queryException) {
      String exceptionMessage = queryException.message

      QueryExecutionException queryExecutionExceptionToThrow =
          new QueryExecutionException("Query execution failed for business reasons (normal execution flow): $exceptionMessage", null, queryException)

      log.debug("Execution of query handler failed for business reasons (normal execution flow): $exceptionMessage", queryExecutionExceptionToThrow)

      throw queryExecutionExceptionToThrow
    }
    catch (Exception e) {
      String detailsExceptionMessage = "Query execution failed because of ${e.getClass().name}"
      if (e.message?.trim()) {
        detailsExceptionMessage += ": ${e.message.trim()}"
      }
      RemoteHandlerException detailsException = new RemoteHandlerException(UUID.randomUUID().toString(), detailsExceptionMessage)

      QueryExecutionException queryExecutionExceptionToThrow =
          new QueryExecutionException("Query execution failed [detailsException.exceptionId: ${detailsException.exceptionId}]", e, detailsException)

      log.error("Execution of query handler failed [detailsException.exceptionId: ${detailsException.exceptionId}]", queryExecutionExceptionToThrow)
      throw queryExecutionExceptionToThrow
    }
  }
}
