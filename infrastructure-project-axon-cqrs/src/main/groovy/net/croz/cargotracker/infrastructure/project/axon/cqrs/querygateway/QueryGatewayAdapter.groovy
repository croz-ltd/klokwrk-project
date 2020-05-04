package net.croz.cargotracker.infrastructure.project.axon.cqrs.querygateway

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryExecutionException
import org.axonframework.queryhandling.QueryGateway

import java.util.concurrent.CompletionException

@CompileStatic
class QueryGatewayAdapter {
  private final QueryGateway queryGateway

  QueryGatewayAdapter(QueryGateway queryGateway) {
    this.queryGateway = queryGateway
  }

  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, Q> R query(OperationRequest<Q> queryOperationRequest, Class<R> queryResponseClass) {
    return query(queryOperationRequest.payload, queryOperationRequest.metaData, queryResponseClass)
  }

  @SuppressWarnings("GrUnnecessaryPublicModifier")
  public <R, Q> R query(Q query, Map<String, ?> metaData, Class<R> queryResponseClass) {
    assert query != null

    GenericMessage queryMessage = new GenericMessage(query, metaData)

    R queryResponse
    try {
      queryResponse = queryGateway.query(query.getClass().name, queryMessage, ResponseTypes.instanceOf(queryResponseClass)).join()
    }
    catch (CompletionException completionException) {
      if (completionException.cause instanceof QueryExecutionException) {
        QueryExecutionException queryExecutionException = completionException.cause as QueryExecutionException
        if (queryExecutionException.details.isPresent()) {
          Throwable detailsThrowable = queryExecutionException.details.get() as Throwable
          throw detailsThrowable
        }

        throw queryExecutionException
      }

      throw completionException
    }

    return queryResponse
  }
}
