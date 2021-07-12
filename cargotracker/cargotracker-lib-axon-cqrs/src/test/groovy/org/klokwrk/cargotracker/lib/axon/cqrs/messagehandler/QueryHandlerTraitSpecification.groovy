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
package org.klokwrk.cargotracker.lib.axon.cqrs.messagehandler

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.slf4j.LoggerFactory
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
      queryExecutionException.message == "Query execution failed: My not found"
      queryExecutionException.cause == null
      details.get() instanceof QueryException
      (details.get() as QueryException).violationInfo.violationCode == ViolationCode.NOT_FOUND
      (details.get() as QueryException).message == "My not found"
    }
  }

  void "doThrow - should throw QueryExecutionException for passed in QueryException with different logging levels"() {
    given:
    Logger logger = LoggerFactory.getLogger(QueryHandlerTrait) as Logger
    logger.level = loggerLevel

    MyQueryHandler myQueryHandler = new MyQueryHandler()

    when:
    myQueryHandler.handleQuery()

    then:
    QueryExecutionException queryExecutionException = thrown(QueryExecutionException)
    verifyAll(queryExecutionException) {
      queryExecutionException.message == "Query execution failed: My not found"
      queryExecutionException.cause == null
      details.get() instanceof QueryException
      (details.get() as QueryException).violationInfo.violationCode == ViolationCode.NOT_FOUND
      (details.get() as QueryException).message == "My not found"
    }

    where:
    loggerLevel | _
    Level.WARN  | _
    Level.DEBUG | _
  }

  void "doThrow - should throw QueryExecutionException for passed in QueryException without message"() {
    given:
    MyQueryHandler myQueryHandler = new MyQueryHandler()

    when:
    myQueryHandler.anotherHandleQuery()

    then:
    QueryExecutionException queryExecutionException = thrown(QueryExecutionException)
    verifyAll(queryExecutionException) {
      queryExecutionException.message == "Query execution failed: ${(details.get() as QueryException).violationInfo.violationCode.codeMessage}"
      queryExecutionException.cause == null
      details.get() instanceof QueryException
      (details.get() as QueryException).violationInfo.violationCode == ViolationCode.NOT_FOUND
      (details.get() as QueryException).message == (details.get() as QueryException).violationInfo.violationCode.codeMessage
    }
  }
}
