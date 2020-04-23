package net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.exception.QueryException
import org.axonframework.queryhandling.QueryExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
trait QueryHandlerTrait extends MessageHandlerTrait {
  static private Logger log = LoggerFactory.getLogger(QueryHandlerTrait.name)

  void doThrow(QueryException domainException) {
    QueryExecutionException queryExecutionException = new QueryExecutionException("query execution failed", new ThrowAwayRuntimeException(), domainException)
    log.debug("Query execution in '${this.getClass().name}' failed.", queryExecutionException)

    throw queryExecutionException
  }
}
