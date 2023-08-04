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
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.HandlerAttributes
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember

import java.lang.reflect.Method

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of event handlers executions.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracker.axon.event-handler-logging</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about aggregate
 * identifier, sequence number, global index and event id.
 * <p/>
 * Logged output looks similar to this (single line in output):
 * <pre>
 * ... cargotracker.axon.event-handler-logging : Executing EventHandler method [BookingOfferSummaryProjectionService.onBookingOfferCreatedEvent(BookingOfferCreatedEvent)]
 *         with event [eventGlobalIndex: 6, eventId: 76e6ea70-4fd8-47f9-a15b-ce8df4a939e2, BookingOfferCreatedEvent(aggregateIdentifier: eaa1efa4-ff9d-4bd8-8e83-4e4b2c1bbcfb, sequenceNumber: 0)]
 * </pre>
 * To register this HandlerEnhancerDefinition, use standard means as described in Axon documentation. Usually this will require adding a simple bean declaration in the Spring Boot config. However,
 * if you have standalone projection app <code>EventHandler</code> annotations are present (not a single <code>CommandHandler</code> or <code>QueryHandler</code> are present), only option at the moment
 * is to specify handler enhancer definition's fully qualified class name in <code>META-INF/services/org.axonframework.messaging.annotation.HandlerEnhancerDefinition</code>.
 * <p/>
 * Logger output contains information about aggregate identifier and sequence number which enables easy correlation with logging outputs produced by {@link LoggingCommandHandlerEnhancerDefinition}
 * and {@link LoggingEventSourcingHandlerEnhancerDefinition}.
 */
@CompileStatic
class LoggingEventHandlerEnhancerDefinition implements HandlerEnhancerDefinition {
  @Override
  <T> MessageHandlingMember<T> wrapHandler(MessageHandlingMember<T> originalMessageHandlingMember) {
    // @formatter:off
    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .attribute(HandlerAttributes.MESSAGE_TYPE)
          .filter({ Class messageType -> messageType == EventMessage })
          .filter({ Class messageType -> !originalMessageHandlingMember.attribute("EventSourcingHandler.payloadType").isPresent() })
          .map({ Class payloadType -> new LoggingEventHandlingMember(originalMessageHandlingMember) as MessageHandlingMember })
        .orElse(originalMessageHandlingMember)
    // @formatter:on

    return selectedMessageHandlingMember
  }

  @Slf4j(category = "cargotracker.axon.event-handler-logging")
  static class LoggingEventHandlingMember<T> extends WrappedMessageHandlingMember<T> {
    MessageHandlingMember<T> messageHandlingMember

    protected LoggingEventHandlingMember(MessageHandlingMember<T> messageHandlingMember) {
      super(messageHandlingMember)
      this.messageHandlingMember = messageHandlingMember
    }

    @Override
    Object handle(Message<?> message, T target) throws Exception {
      if (log.isDebugEnabled()) {
        messageHandlingMember
            .unwrap(Method)
            .ifPresent({ Method method ->
              String eventGlobalIndexString = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(message)
              String eventAggregateIdentifier = AxonMessageHelper.fetchAggregateIdentifierIfPossible(message)
              String eventSequenceNumber = AxonMessageHelper.fetchSequenceNumberAsStringIfPossible(message)

              String eventOutput = "eventGlobalIndex: ${eventGlobalIndexString}, eventId: ${message.identifier}, ${message.payloadType.simpleName}" +
                                   "(aggregateIdentifier: ${eventAggregateIdentifier}, " +
                                   "sequenceNumber: ${eventSequenceNumber})"

              String debugMessage = "Executing EventHandler method [${method.declaringClass.simpleName}.${method.name}(${method.parameterTypes*.simpleName.join(",")})] with event [$eventOutput]"
              log.debug(debugMessage)
            })
      }

      return super.handle(message, target)
    }
  }
}
