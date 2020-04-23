package net.croz.cargotracker.infrastructure.shared.axon.logging

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.GapAwareTrackingToken
import org.axonframework.eventhandling.GenericTrackedDomainEventMessage
import org.axonframework.eventhandling.GlobalSequenceTrackingToken
import org.axonframework.eventhandling.TrackingToken
import org.axonframework.messaging.Message

@CompileStatic
class AxonMessageHelper {
  static String fetchGlobalIndexAsStringIfPossible(Message<?> message, String nonExistingPlaceholder = "n/a") {
    return fetchGlobalIndexIfPossible(message) ?: nonExistingPlaceholder
  }

  static Long fetchGlobalIndexIfPossible(Message<?> message) {
    Long eventGlobalIndex = null

    if (message instanceof GenericTrackedDomainEventMessage) {
      TrackingToken trackingToken = message.trackingToken()
      if (trackingToken instanceof GapAwareTrackingToken) {
        eventGlobalIndex = (trackingToken as GapAwareTrackingToken).index
      }
      else if (trackingToken instanceof GlobalSequenceTrackingToken) {
        eventGlobalIndex = (trackingToken as GlobalSequenceTrackingToken).globalIndex
      }
    }

    return eventGlobalIndex
  }

  static String fetchAggregateIdentifierIfPossible(Message<?> message, String nonExistingPlaceholder = "n/a") {
    String eventAggregateIdentifier = nonExistingPlaceholder
    Object event = message.payload

    if (event?.hasProperty("aggregateIdentifier")) {
      eventAggregateIdentifier = event["aggregateIdentifier"]
    }

    if (eventAggregateIdentifier == nonExistingPlaceholder && message.hasProperty("aggregateIdentifier")) {
      eventAggregateIdentifier = message["aggregateIdentifier"]
    }

    return eventAggregateIdentifier
  }

  static String fetchSequenceNumberAsStringIfPossible(Message<?> message, String nonExistingPlaceholder = "n/a") {
    String eventSequenceNumber = message.hasProperty("sequenceNumber") ? message["sequenceNumber"] : nonExistingPlaceholder
    return eventSequenceNumber
  }
}
