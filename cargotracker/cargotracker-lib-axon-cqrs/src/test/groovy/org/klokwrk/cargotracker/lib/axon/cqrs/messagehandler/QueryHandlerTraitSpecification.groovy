package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationInfo
import spock.lang.Specification

class QueryHandlerTraitSpecification extends Specification {

  class MyQueryHandler implements QueryHandlerTrait {
    void handleQuery() {
      doThrow(new QueryException(ViolationInfo.NOT_FOUND, "My not found"))
    }
  }

  void "doThrow - should throw QueryExecutionException for passed in QueryException"() {
    given:
    MyQueryHandler myQueryHandler = new MyQueryHandler()

    when:
    myQueryHandler.handleQuery()

    then:
    QueryExecutionException queryExecutionException = thrown(QueryExecutionException)
    verifyAll(queryExecutionException) {
      queryExecutionException.cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException
      details.get() instanceof QueryException
      (details.get() as QueryException).violationInfo.violationCode == ViolationCode.NOT_FOUND
    }
  }
}
