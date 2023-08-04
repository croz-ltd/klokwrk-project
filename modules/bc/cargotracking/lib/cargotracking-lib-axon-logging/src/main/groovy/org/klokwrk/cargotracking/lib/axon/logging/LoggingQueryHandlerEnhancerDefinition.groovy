/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.lib.axon.logging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.messaging.HandlerAttributes
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember
import org.axonframework.queryhandling.QueryMessage

import java.lang.reflect.Method

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of query handler executions.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracker.axon.query-handler-logging</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about query's payload.
 * <p/>
 * Logged output looks similar to this:
 * <pre>
 * ... cargotracker.axon.query-handler-logging - Executing QueryHandler method [MyTestQueryHandler.handleSomeQuery(MyTestQuery)] with payload [query:123]
 * </pre>
 * To register this HandlerEnhancerDefinition, use standard means as described in Axon documentation. In Spring Boot applications only a simple bean declaration is required.
 */
@CompileStatic
class LoggingQueryHandlerEnhancerDefinition implements HandlerEnhancerDefinition {
  @Override
  <T> MessageHandlingMember<T> wrapHandler(MessageHandlingMember<T> originalMessageHandlingMember) {
    // @formatter:off
    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .attribute(HandlerAttributes.MESSAGE_TYPE)
          .filter({ Class messageType -> messageType == QueryMessage })
          .map({ Class messageType -> new LoggingQueryHandlingMember(originalMessageHandlingMember) as MessageHandlingMember })
        .orElse(originalMessageHandlingMember)
    // @formatter:on

    return selectedMessageHandlingMember
  }

  @Slf4j(category = "cargotracker.axon.query-handler-logging")
  static class LoggingQueryHandlingMember<T> extends WrappedMessageHandlingMember<T> {
    MessageHandlingMember<T> messageHandlingMember

    protected LoggingQueryHandlingMember(MessageHandlingMember<T> messageHandlingMember) {
      super(messageHandlingMember)
      this.messageHandlingMember = messageHandlingMember
    }

    @Override
    Object handle(Message<?> message, T target) throws Exception {
      if (log.isDebugEnabled()) {
        messageHandlingMember.unwrap(Method).ifPresent({ Method method ->
          Map payloadProperties = message.payload.propertiesFiltered
          String debugMessage = "Executing QueryHandler method [${method.declaringClass.simpleName}.${method.name}(${method.parameterTypes*.simpleName.join(",")})] with payload ${payloadProperties}"
          log.debug(debugMessage)
        })
      }

      return super.handle(message, target)
    }
  }
}
