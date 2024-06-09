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
package org.klokwrk.cargotracking.lib.axon.errorhandling

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.eventhandling.ErrorContext
import org.axonframework.eventhandling.ErrorHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.EventMessageHandler
import org.axonframework.eventhandling.EventProcessingException
import org.axonframework.eventhandling.ListenerInvocationErrorHandler

/**
 * Event processing error handler providing logging and propagating behavior.
 * <p/>
 * Out-of-the-box, Axon Framework provides {@code org.axonframework.eventhandling.PropagatingErrorHandler} and {@code org.axonframework.eventhandling.LoggingErrorHandler}. However, the most useful
 * event processing error handler would be the combination of the two, and this is exactly what this class provides.
 */
@Slf4j("logger")
@CompileStatic
class PropagatingAndLoggingErrorHandler implements ErrorHandler, ListenerInvocationErrorHandler {
  @Override
  void onError(Exception exception, EventMessage<?> event, EventMessageHandler eventHandler) throws Exception {
    logger.error("Exception occured in EventListener [${ eventHandler.targetType.simpleName }] for [${ event.payloadType.simpleName } - ${ event.identifier }]. Failed event: [${ event }].", exception)
    throw exception
  }

  @SuppressWarnings("CodeNarc.Instanceof")
  @Override
  void handleError(ErrorContext errorContext) throws Exception {
    Throwable error = errorContext.error()

    if (error instanceof Error) {
      logger.error("Error occurred during event processing in [${ errorContext.eventProcessor() }] processor. Failed events: [${ errorContext.failedEvents() }]", error)
      throw (Error) error
    }
    else if (error instanceof Exception) {
      // For exceptions, error logging is already handled in onError() method
      throw (Exception) error
    }
    else {
      logger.error("Error occurred during event processing in [${ errorContext.eventProcessor() }] processor. Failed events: [${ errorContext.failedEvents() }]", error)
      throw new EventProcessingException("An error occurred while handling an event", error)
    }
  }
}
