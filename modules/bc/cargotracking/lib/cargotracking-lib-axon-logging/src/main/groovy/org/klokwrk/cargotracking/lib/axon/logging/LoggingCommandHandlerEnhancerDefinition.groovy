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
package org.klokwrk.cargotracking.lib.axon.logging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.HandlerAttributes
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember

import java.lang.reflect.Constructor
import java.lang.reflect.Method

import static AxonMessageHelper.AGGREGATE_IDENTIFIER
import static AxonMessageHelper.SEQUENCE_NUMBER
import static org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants.NOT_AVAILABLE

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of command handlers executions.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracking-lib-axon-logging.command-handler-logger</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about
 * aggregate identifier and sequence number.
 * <p/>
 * Logged output looks similar to this (single line in output):
 * <pre>
 * ... cargotracking-lib-axon-logging.command-handler-logger : Executing CommandHandler constructor [PredmetAggregate(CreatePredmetCommand,PredmetClassificationDomainService)]
 *         with command [CreatePredmetCommand(aggregateIdentifier: 0eb588a9-cfeb-4be5-8ea9-04c9d14b7df9)]
 * </pre>
 * To register this HandlerEnhancerDefinition, use standard means as described in Axon documentation. In Spring Boot applications only a simple bean declaration is required.
 * <p/>
 * Logger output contains information about aggregate identifier and sequence number which enables easy correlation with logging outputs produced by {@link LoggingEventHandlerEnhancerDefinition}
 * and {@link LoggingEventSourcingHandlerEnhancerDefinition}.
 */
@CompileStatic
class LoggingCommandHandlerEnhancerDefinition implements HandlerEnhancerDefinition {
  @Override
  <T> MessageHandlingMember<T> wrapHandler(MessageHandlingMember<T> originalMessageHandlingMember) {
    // @formatter:off
    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .attribute(HandlerAttributes.MESSAGE_TYPE)
          .filter({ Class messageType -> messageType == CommandMessage })
          .map({ Class messageType -> new LoggingCommandHandlingMember(originalMessageHandlingMember) as MessageHandlingMember })
        .orElse(originalMessageHandlingMember)
    // @formatter:on

    return selectedMessageHandlingMember
  }

  @Slf4j(category = "cargotracking-lib-axon-logging.command-handler-logger")
  static class LoggingCommandHandlingMember<T> extends WrappedMessageHandlingMember<T> {
    MessageHandlingMember<T> messageHandlingMember

    protected LoggingCommandHandlingMember(MessageHandlingMember<T> messageHandlingMember) {
      super(messageHandlingMember)
      this.messageHandlingMember = messageHandlingMember
    }

    @Override
    Object handle(Message<?> message, T target) throws Exception {
      if (log.isDebugEnabled()) {
        // Logging for a method annotated with @CommandHandler
        messageHandlingMember.unwrap(Method).ifPresent({ Method method ->
          Object command = message.payload
          String commandAggregateIdentifier = command.hasProperty(AGGREGATE_IDENTIFIER) ? command[AGGREGATE_IDENTIFIER] : NOT_AVAILABLE
          String commandSequenceNumber = command.hasProperty(SEQUENCE_NUMBER) ? command[SEQUENCE_NUMBER] : NOT_AVAILABLE
          String commandOutput = "${ command.getClass().simpleName }(aggregateIdentifier: ${ commandAggregateIdentifier }, sequenceNumber: ${ commandSequenceNumber })"

          String debugMessage =
              "Executing CommandHandler method [${ method.declaringClass.simpleName }.${ method.name }(${ method.parameterTypes*.simpleName.join(",") })] with command [$commandOutput]"
          log.debug(debugMessage)
        })

        // Logging for a constructor annotated with @CommandHandler
        messageHandlingMember.unwrap(Constructor).ifPresent({ Constructor executable ->
          Object command = message.payload
          String commandAggregateIdentifier = command.hasProperty(AGGREGATE_IDENTIFIER) ? command[AGGREGATE_IDENTIFIER] : NOT_AVAILABLE
          String commandOutput = "${ command.getClass().simpleName }(aggregateIdentifier: ${ commandAggregateIdentifier })"

          String debugMessage = "Executing CommandHandler constructor [${ executable.declaringClass.simpleName }(${ executable.parameterTypes*.simpleName.join(",") })] with command [$commandOutput]"
          log.debug(debugMessage)
        })
      }

      return super.handle(message, target)
    }
  }
}
