package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.exception.QueryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simplifies some aspects of Axon API usage during query handling.
 */
@CompileStatic
trait QueryHandlerTrait extends MessageHandlerTrait {
  static private final Logger log = LoggerFactory.getLogger(QueryHandlerTrait.name)

  /**
   * Simplifies throwing a business exception making sure it is propagated back to the caller as a details field of Axon's <code>QueryExecutionException</code>.
   * <p/>
   * It also logs the stacktrace of QueryExecutionException being thrown, which helps during development.
   */
  void doThrow(QueryException domainException) {
    QueryExecutionException queryExecutionException = new QueryExecutionException("query execution failed", new ThrowAwayRuntimeException(), domainException)
    log.debug("Query execution in '${this.getClass().name}' failed.", queryExecutionException)

    throw queryExecutionException
  }
}
