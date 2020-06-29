package org.klokwrk.cargotracker.booking.queryside.test.axon

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.lib.axon.api.event.BaseEvent

@CompileStatic
class GenericDomainEventMessageFactory {
  static <T extends BaseEvent> GenericDomainEventMessage createEventMessage(T event, Map<String, ?> metadataMap, Long sequenceNumber = 0) {
    GenericDomainEventMessage<T> eventMessage = new GenericDomainEventMessage<>(event.getClass().simpleName, event.aggregateIdentifier, sequenceNumber, event, metadataMap)
    return eventMessage
  }
}
