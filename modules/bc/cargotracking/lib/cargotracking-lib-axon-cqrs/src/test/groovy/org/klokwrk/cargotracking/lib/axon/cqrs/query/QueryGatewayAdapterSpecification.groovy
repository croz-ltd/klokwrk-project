/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.queryhandling.QueryExecutionException
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class QueryGatewayAdapterSpecification extends Specification {
  QueryGateway queryGatewayMock
  QueryGatewayAdapter queryGatewayAdapter

  void setup() {
    queryGatewayMock = Mock()
    queryGatewayAdapter = new QueryGatewayAdapter(queryGatewayMock)
  }

  void "query(operationRequest, responseClass) - should behave same as query(query, metaData, responseClass)"() {
    given:
    def query = "query"
    Map<String, ?> metaData = [:]

    CompletableFuture<String> queryResultCompletableFuture = new CompletableFuture<>()
    queryResultCompletableFuture.complete("query result")

    when:
    queryGatewayAdapter.query(new OperationRequest(payload: query, metaData: metaData), String)

    then:
    1 * queryGatewayMock.query(
        _ as String,
        { def queryMessage ->
          verifyAll {
            queryMessage instanceof GenericMessage
            queryMessage.payload == query
            queryMessage.metaData == metaData
          }
        },
        _ as ResponseType
    ) >> queryResultCompletableFuture
  }

  void "query(query, metaData, responseClass) - should fail for null query"() {
    given:
    def query = null
    Map<String, ?> metaData = [:]

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    thrown(AssertionError)
  }

  void "query(query, metaData, responseClass) - should work null metaData"() {
    given:
    def query = "query"
    Map<String, ?> metaData = null

    CompletableFuture<String> queryResultCompletableFuture = new CompletableFuture<>()
    queryResultCompletableFuture.complete("query result")

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    1 * queryGatewayMock.query(
        _ as String,
        { def queryMessage ->
          verifyAll {
            queryMessage instanceof GenericMessage
            queryMessage.payload == query
            queryMessage.metaData === MetaData.emptyInstance()
          }
        },
        _ as ResponseType
    ) >> queryResultCompletableFuture
  }

  void "query(query, metaData, responseClass) - should fail for null responseClass"() {
    given:
    def query = "query"
    Map<String, ?> metaData = [:]

    when:
    queryGatewayAdapter.query(query, metaData, null)

    then:
    thrown(IllegalArgumentException)
  }

  void "query(query, metaData, responseClass) - should delegate to the query gateway"() {
    given:
    def query = "query"
    Map<String, ?> metaData = [:]

    CompletableFuture<String> queryResultCompletableFuture = new CompletableFuture<>()
    queryResultCompletableFuture.complete("query result")

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    1 * queryGatewayMock.query(
        _ as String,
        { def queryMessage ->
          verifyAll {
            queryMessage instanceof GenericMessage
            queryMessage.payload == query
            queryMessage.metaData == metaData
          }
        },
        _ as ResponseType
    ) >> queryResultCompletableFuture
  }

  void "query(query, metaData, responseClass) - should propagate CompletionException when cause is not QueryExecutionException"() {
    given:
    CompletionException completionException = new CompletionException("completion exception", new RuntimeException())

    QueryGateway queryGatewayStub = Stub()
    queryGatewayStub.query(_ as String, _, _ as ResponseType) >> { throw completionException }

    QueryGatewayAdapter queryGatewayAdapter = new QueryGatewayAdapter(queryGatewayStub)

    def query = "query"
    Map<String, ?> metaData = [:]

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    thrown(CompletionException)
  }

  void "query(query, metaData, responseClass) - should propagate QueryExecutionException when details exception is not available"() {
    given:
    QueryExecutionException queryExecutionException = new QueryExecutionException("Query execution failed", null)
    CompletionException completionException = new CompletionException("completion exception", queryExecutionException)

    QueryGateway queryGatewayStub = Stub()
    queryGatewayStub.query(_ as String, _, _ as ResponseType) >> { throw completionException }

    QueryGatewayAdapter queryGatewayAdapter = new QueryGatewayAdapter(queryGatewayStub)

    def query = "query"
    Map<String, ?> metaData = [:]

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    thrown(QueryExecutionException)
  }

  class MyException extends RuntimeException {
    MyException(String message) {
      super(message)
    }
  }

  void "query(query, metaData, responseClass) - should propagate details exception when it is available [details exception class: #exceptionDetailsParam.getClass().simpleName]"() {
    given:
    QueryExecutionException queryExecutionException = new QueryExecutionException("Query execution failed", null, exceptionDetailsParam)
    CompletionException completionException = new CompletionException("completion exception", queryExecutionException)

    QueryGateway queryGatewayStub = Stub()
    queryGatewayStub.query(_ as String, _, _ as ResponseType) >> { throw completionException }

    QueryGatewayAdapter queryGatewayAdapter = new QueryGatewayAdapter(queryGatewayStub)

    def query = "query"
    Map<String, ?> metaData = [:]

    when:
    queryGatewayAdapter.query(query, metaData, String)

    then:
    //noinspection GroovyAssignabilityCheck
    thrown(exceptionDetailsParam.getClass())

    where:
    exceptionDetailsParam           | _
    new MyException("my exception") | _
    new QueryException()            | _
  }
}
