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

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryExecutionException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simplifies some aspects of Axon API usage during query handling.
 */
@CompileStatic
trait QueryHandlerTrait extends MessageHandlerTrait {
  // Note: @Slf4j AST transformation does not work with traits
  static private final Logger log = LoggerFactory.getLogger(QueryHandlerTrait.name)

  /**
   * Simplifies throwing a business exception making sure it is propagated back to the caller as a details field of Axon's <code>QueryExecutionException</code>.
   * <p/>
   * It also logs the stacktrace of QueryExecutionException being thrown, which helps during development.
   */
  void doThrow(QueryException domainException) {
    String exceptionMessage = domainException.message ?: domainException.violationInfo.violationCode.codeMessage
    QueryExecutionException queryExecutionException = new QueryExecutionException("Query execution failed: $exceptionMessage", null, domainException)
    if (log.isDebugEnabled()) {
      log.debug("Query execution in '${this.getClass().name}' failed: $exceptionMessage", queryExecutionException)
    }

    throw queryExecutionException
  }
}
