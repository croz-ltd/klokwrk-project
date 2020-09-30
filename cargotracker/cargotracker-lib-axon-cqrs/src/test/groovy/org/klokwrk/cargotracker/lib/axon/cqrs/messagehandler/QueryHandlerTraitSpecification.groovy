package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import spock.lang.Specification

class QueryHandlerTraitSpecification extends Specification {

  class MyQueryHandler implements QueryHandlerTrait {
    void handleQuery() {
      doThrow(new QueryException(ViolationInfo.NOT_FOUND, "My not found"))
    }

    void anotherHandleQuery() {
      doThrow(new QueryException(ViolationInfo.NOT_FOUND, null))
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

  void "doThrow - should throw QueryExecutionException for passed in QueryException without message"() {
    given:
    MyQueryHandler myQueryHandler = new MyQueryHandler()

    when:
    myQueryHandler.anotherHandleQuery()

    then:
    QueryExecutionException queryExecutionException = thrown(QueryExecutionException)
    verifyAll(queryExecutionException) {
      queryExecutionException.cause instanceof MessageHandlerTrait.ThrowAwayRuntimeException
      details.get() instanceof QueryException
      (details.get() as QueryException).violationInfo.violationCode == ViolationCode.NOT_FOUND
      (details.get() as QueryException).message == (details.get() as QueryException).violationInfo.violationCode.codeMessage
    }
  }
}
