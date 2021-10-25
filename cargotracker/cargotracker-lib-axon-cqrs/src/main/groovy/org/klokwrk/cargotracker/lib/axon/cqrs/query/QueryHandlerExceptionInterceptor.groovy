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
  @SuppressWarnings(["CodeNarc.CatchException", 'DuplicatedCode'])
  @Override
  Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain) throws Exception {
    try {
      Object returnValue = interceptorChain.proceed()
      return returnValue
    }
    catch (QueryException queryException) {
      String queryTypeName = unitOfWork.message.payloadType.simpleName
      String exceptionMessage = queryException.message

      QueryExecutionException queryExecutionExceptionToThrow =
          new QueryExecutionException("Execution of '$queryTypeName' query failed for business reasons (normal execution flow): $exceptionMessage", null, queryException)

      log.debug("Execution of '$queryTypeName' query handler failed for business reasons (normal execution flow): $exceptionMessage")

      throw queryExecutionExceptionToThrow
    }
    catch (Exception e) {
      String queryTypeName = unitOfWork.message.payloadType.simpleName
      String detailsExceptionMessage = "Execution of '$queryTypeName' query failed because of ${e.getClass().name}"
      if (e.message?.trim()) {
        detailsExceptionMessage += ": ${e.message.trim()}"
      }
      RemoteHandlerException detailsException = new RemoteHandlerException(UUID.randomUUID().toString(), detailsExceptionMessage)

      QueryExecutionException queryExecutionExceptionToThrow =
          new QueryExecutionException("Execution of '$queryTypeName' query failed [detailsException.exceptionId: ${detailsException.exceptionId}]", e, detailsException)

      log.error("Execution of query handler failed [detailsException.exceptionId: ${detailsException.exceptionId}]", queryExecutionExceptionToThrow)
      throw queryExecutionExceptionToThrow
    }
  }
}
