package org.klokwrk.cargotracker.lib.axon.logging

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.GapAwareTrackingToken
import org.axonframework.eventhandling.GenericTrackedDomainEventMessage
import org.axonframework.eventhandling.GlobalSequenceTrackingToken
import org.axonframework.eventhandling.TrackingToken
import org.axonframework.messaging.Message

import static net.croz.cargotracker.lang.groovy.constant.CommonConstants.NOT_AVAILABLE

@CompileStatic
class AxonMessageHelper {
  // TODO dmurat: If the need arises, following two constants should go into standalone module (module could be named something like infrastructure-project-axon-base or
  //              infrastructure-project-axon-constant)
  static final String AGGREGATE_IDENTIFIER = "aggregateIdentifier"
  static final String SEQUENCE_NUMBER = "sequenceNumber"

  static String fetchGlobalIndexAsStringIfPossible(Message<?> message, String nonExistingPlaceholder = NOT_AVAILABLE) {
    return fetchGlobalIndexIfPossible(message) ?: nonExistingPlaceholder
  }

  @SuppressWarnings("Instanceof")
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

  static String fetchAggregateIdentifierIfPossible(Message<?> message, String nonExistingPlaceholder = NOT_AVAILABLE) {
    String eventAggregateIdentifier = nonExistingPlaceholder
    Object event = message.payload

    if (event?.hasProperty(AGGREGATE_IDENTIFIER)) {
      eventAggregateIdentifier = event[AGGREGATE_IDENTIFIER]
    }

    if (eventAggregateIdentifier == nonExistingPlaceholder && message.hasProperty(AGGREGATE_IDENTIFIER)) {
      eventAggregateIdentifier = message[AGGREGATE_IDENTIFIER]
    }

    return eventAggregateIdentifier
  }

  static String fetchSequenceNumberAsStringIfPossible(Message<?> message, String nonExistingPlaceholder = NOT_AVAILABLE) {
    String eventSequenceNumber = message.hasProperty(SEQUENCE_NUMBER) ? message[SEQUENCE_NUMBER] : nonExistingPlaceholder
    return eventSequenceNumber
  }
}
