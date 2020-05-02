package net.croz.cargotracker.infrastructure.project.axon.logging

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.messaging.annotation.MessageHandlingMember
import org.axonframework.messaging.annotation.WrappedMessageHandlingMember

import java.lang.reflect.Method

/**
 * Defines Axon's {@link HandlerEnhancerDefinition} for detailed logging of event sourcing handlers executions in projectors.
 * <p/>
 * Corresponding Slf4j logger uses '<code>cargotracker.axon.eventsourcing-handler-logging</code>' category and it logs on <code>DEBUG</code> level. Logger output contains information about event's
 * aggregate identifier and event's sequence number.
 * <p/>
 * Logged output looks similar to this (single line in output):
 * <pre>
 * ... cargotracker.axon.eventsourcing-handler-logging : Executing EventSourcingHandler method [PredmetAggregate.onPredmetCreatedEvent(PredmetCreatedEvent,DomainEventMessage)]
 *         with event [eventId: 454dbf65-6686-4529-92c3-e973ccd6ece3, PredmetCreatedEvent(aggregateIdentifier: 0eb588a9-cfeb-4be5-8ea9-04c9d14b7df9, sequenceNumber: 0)]
 * </pre>
 * To register this HandlerEnhancerDefinition, use standard means as described in Axon documentation. In Spring Boot applications only a simple bean declaration is required.
 * <p/>
 * Logger output contains information about aggregate identifier and sequence number which enables easy correlation with logging outputs produced by {@link LoggingCommandHandlerEnhancerDefinition}
 * and {@link LoggingEventHandlerEnhancerDefinition}.
 */
@CompileStatic
class LoggingEventSourcingHandlerEnhancerDefinition implements HandlerEnhancerDefinition {
  @Override
  <T> MessageHandlingMember<T> wrapHandler(MessageHandlingMember<T> originalMessageHandlingMember) {
    MessageHandlingMember selectedMessageHandlingMember = originalMessageHandlingMember
        .annotationAttributes(EventSourcingHandler)
        .map((Map<String, Object> attr) -> new LoggingEventSourcingHandlingMember(originalMessageHandlingMember) as MessageHandlingMember)
        .orElse(originalMessageHandlingMember) as MessageHandlingMember

    return selectedMessageHandlingMember
  }

  @Slf4j(category = "cargotracker.axon.eventsourcing-handler-logging")
  static class LoggingEventSourcingHandlingMember<T> extends WrappedMessageHandlingMember<T> {
    MessageHandlingMember<T> messageHandlingMember

    protected LoggingEventSourcingHandlingMember(MessageHandlingMember<T> messageHandlingMember) {
      super(messageHandlingMember)
      this.messageHandlingMember = messageHandlingMember
    }

    @Override
    Object handle(Message<?> message, T target) throws Exception {
      if (log.isDebugEnabled()) {
        messageHandlingMember.unwrap(Method).ifPresent((Method method) -> {
          Object event = message.payload

          String eventAggregateIdentifier = AxonMessageHelper.fetchAggregateIdentifierIfPossible(message)
          String eventSequenceNumber = AxonMessageHelper.fetchSequenceNumberAsStringIfPossible(message)

          String eventOutput = "eventId: ${message.identifier}, ${event?.getClass()?.simpleName}(aggregateIdentifier: ${eventAggregateIdentifier}, sequenceNumber: ${eventSequenceNumber})"

          log.debug("Executing EventSourcingHandler method [${method.declaringClass.simpleName}.${method.name}(${method.parameterTypes*.simpleName?.join(",")})] with event [$eventOutput]")
        })
      }

      return super.handle(message, target)
    }
  }
}
