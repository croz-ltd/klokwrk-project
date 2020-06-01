package org.klokwrk.cargotracker.lib.axon.logging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember

import java.lang.reflect.Constructor
import java.lang.reflect.Method

import static AxonMessageHelper.AGGREGATE_IDENTIFIER
import static AxonMessageHelper.SEQUENCE_NUMBER
import static org.klokwrk.lang.groovy.constant.CommonConstants.NOT_AVAILABLE

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of command handlers executions in projectors.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracker.axon.command-handler-logging</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about aggregate
 * identifier and sequence number.
 * <p/>
 * Logged output looks similar to this (single line in output):
 * <pre>
 * ... cargotracker.axon.command-handler-logging : Executing CommandHandler constructor [PredmetAggregate(CreatePredmetCommand,PredmetClassificationDomainService)]
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
    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .annotationAttributes(CommandHandler)
        .map((Map<String, Object> attr) -> new LoggingCommandHandlingMember(originalMessageHandlingMember) as MessageHandlingMember)
        .orElse(originalMessageHandlingMember) as MessageHandlingMember

    return selectedMessageHandlingMember
  }

  @Slf4j(category = "cargotracker.axon.command-handler-logging")
  static class LoggingCommandHandlingMember<T> extends WrappedMessageHandlingMember<T> {
    MessageHandlingMember<T> messageHandlingMember

    protected LoggingCommandHandlingMember(MessageHandlingMember<T> messageHandlingMember) {
      super(messageHandlingMember)
      this.messageHandlingMember = messageHandlingMember
    }

    @SuppressWarnings("DuplicateStringLiteral") // TODO dmurat: remove if https://github.com/CodeNarc/CodeNarc/issues/490 gets fixed.
    @Override
    Object handle(Message<?> message, T target) throws Exception {
      if (log.isDebugEnabled()) {
        // Logging for a method annotated with @CommandHandler
        messageHandlingMember.unwrap(Method).ifPresent((Method method) -> {
          Object command = message.payload
          String commandAggregateIdentifier = command.hasProperty(AGGREGATE_IDENTIFIER) ? command[AGGREGATE_IDENTIFIER] : NOT_AVAILABLE
          String commandSequenceNumber = command.hasProperty(SEQUENCE_NUMBER) ? command[SEQUENCE_NUMBER] : NOT_AVAILABLE
          String commandOutput = "${ command.getClass().simpleName }(aggregateIdentifier: ${ commandAggregateIdentifier }, sequenceNumber: ${ commandSequenceNumber })"

          log.debug("Executing CommandHandler method [${ method.declaringClass.simpleName }.${ method.name }(${ method.parameterTypes*.simpleName?.join(",") })] with command [$commandOutput]")
        })

        // Logging for a constructor annotated with @CommandHandler
        messageHandlingMember.unwrap(Constructor).ifPresent({ Constructor executable ->
          Object command = message.payload
          String commandAggregateIdentifier = command.hasProperty(AGGREGATE_IDENTIFIER) ? command[AGGREGATE_IDENTIFIER] : NOT_AVAILABLE
          String commandOutput = "${ command.getClass().simpleName }(aggregateIdentifier: ${ commandAggregateIdentifier })"

          log.debug("Executing CommandHandler constructor [${ executable.declaringClass.simpleName }(${ executable.parameterTypes*.simpleName?.join(",") })] with command [$commandOutput]")
        })
      }

      return super.handle(message, target)
    }
  }
}
