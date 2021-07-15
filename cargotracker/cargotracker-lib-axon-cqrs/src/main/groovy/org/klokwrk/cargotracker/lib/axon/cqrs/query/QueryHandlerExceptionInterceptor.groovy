package org.klokwrk.cargotracker.lib.axon.cqrs.query

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException

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
      QueryExecutionException queryExecutionExceptionToThrow = new QueryExecutionException("Query execution failed.", e, new DomainException())

      log.error("Execution of query handler failed.", queryExecutionExceptionToThrow)
      throw queryExecutionExceptionToThrow
    }
  }
}
