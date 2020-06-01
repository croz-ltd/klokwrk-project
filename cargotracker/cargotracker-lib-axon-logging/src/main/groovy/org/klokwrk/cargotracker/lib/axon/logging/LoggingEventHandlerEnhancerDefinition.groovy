package org.klokwrk.cargotracker.lib.axon.logging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember

import java.lang.reflect.Method

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of event handlers executions in projectors.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracker.axon.event-handler-logging</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about aggregate
 * identifier, sequence number, global index and event id.
 * <p/>
 * Logged output looks similar to this (single line in output):
 * <pre>
 * ... cargotracker.axon.event-handler-logging : Executing EventHandler method [CargoSummaryProjectorService.onCargoBookedEvent(CargoBookedEvent)]
 *         with event [eventGlobalIndex: 6, eventId: 76e6ea70-4fd8-47f9-a15b-ce8df4a939e2, CargoBookedEvent(aggregateIdentifier: eaa1efa4-ff9d-4bd8-8e83-4e4b2c1bbcfb, sequenceNumber: 0)]
 * </pre>
 * To register this HandlerEnhancerDefinition, use standard means as described in Axon documentation. Usually this will require adding a simple bean declaration in the Spring Boot config. However,
 * if you have standalone projector app <code>EventHandler</code> annotations are present (not a single <code>CommandHandler</code> or <code>QueryHandler</code> are present), only option at the moment
 * is to specify handler enhancer definition's fully qualified class name in <code>META-INF/services/org.axonframework.messaging.annotation.HandlerEnhancerDefinition</code>.
 * <p/>
 * Logger output contains information about aggregate identifier and sequence number which enables easy correlation with logging outputs produced by {@link LoggingCommandHandlerEnhancerDefinition}
 * and {@link LoggingEventSourcingHandlerEnhancerDefinition}.
 */
@CompileStatic
class LoggingEventHandlerEnhancerDefinition implements HandlerEnhancerDefinition {
  @Override
  <T> MessageHandlingMember<T> wrapHandler(MessageHandlingMember<T> originalMessageHandlingMember) {
    if (originalMessageHandlingMember.hasAnnotation(EventSourcingHandler)) {
      return originalMessageHandlingMember
    }

    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .annotationAttributes(EventHandler)
        .map((Map<String, Object> attr) -> new LoggingEventHandlingMember(originalMessageHandlingMember) as MessageHandlingMember)
        .orElse(originalMessageHandlingMember) as MessageHandlingMember

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
            .ifPresent((Method method) -> {
              Object event = message.payload

              String eventGlobalIndexString = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(message)
              String eventAggregateIdentifier = AxonMessageHelper.fetchAggregateIdentifierIfPossible(message)
              String eventSequenceNumber = AxonMessageHelper.fetchSequenceNumberAsStringIfPossible(message)

              String eventOutput = "eventGlobalIndex: ${eventGlobalIndexString}, eventId: ${message.identifier}, ${event?.getClass()?.simpleName}" +
                                   "(aggregateIdentifier: ${eventAggregateIdentifier}, " +
                                   "sequenceNumber: ${eventSequenceNumber})"

              log.debug("Executing EventHandler method [${method.declaringClass.simpleName}.${method.name}(${method.parameterTypes*.simpleName?.join(",")})] with event [$eventOutput]")
            })
      }

      return super.handle(message, target)
    }
  }
}
