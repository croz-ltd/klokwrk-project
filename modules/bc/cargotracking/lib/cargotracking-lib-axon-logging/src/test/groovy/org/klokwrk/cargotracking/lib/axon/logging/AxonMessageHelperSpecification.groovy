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

import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.GapAwareTrackingToken
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.eventhandling.GenericTrackedDomainEventMessage
import org.axonframework.eventhandling.GlobalSequenceTrackingToken
import org.axonframework.messaging.GenericMessage
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import spock.lang.Specification

class AxonMessageHelperSpecification extends Specification {
  void "fetchGlobalIndexAsStringIfPossible - should work when global index is available - GapAwareTrackingToken"() {
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 1, "payload")
    GapAwareTrackingToken gapAwareTrackingToken = new GapAwareTrackingToken(10, [])
    GenericTrackedDomainEventMessage genericTrackedDomainEventMessage = new GenericTrackedDomainEventMessage(gapAwareTrackingToken, domainEventMessage)

    when:
    String globalIndex = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(genericTrackedDomainEventMessage)

    then:
    globalIndex == "10"
  }

  void "fetchGlobalIndexAsStringIfPossible - should work when global index is available - GlobalSequenceTrackingToken"() {
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 1, "payload")
    GlobalSequenceTrackingToken globalSequenceTrackingToken = new GlobalSequenceTrackingToken(10)
    GenericTrackedDomainEventMessage genericTrackedDomainEventMessage = new GenericTrackedDomainEventMessage(globalSequenceTrackingToken, domainEventMessage)

    when:
    String globalIndex = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(genericTrackedDomainEventMessage)

    then:
    globalIndex == "10"
  }

  void "fetchGlobalIndexAsStringIfPossible - should work when global index is zero - GlobalSequenceTrackingToken"() {
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 0, "payload")
    GlobalSequenceTrackingToken globalSequenceTrackingToken = new GlobalSequenceTrackingToken(0)
    GenericTrackedDomainEventMessage genericTrackedDomainEventMessage = new GenericTrackedDomainEventMessage(globalSequenceTrackingToken, domainEventMessage)

    when:
    String globalIndex = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(genericTrackedDomainEventMessage)

    then:
    globalIndex == "0"
  }

  void "fetchGlobalIndexAsStringIfPossible - should work when global index is NOT available - GenericMessage"() {
    given:
    GenericMessage genericMessage = new GenericMessage(null)

    when:
    String globalIndex = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(genericMessage)

    then:
    globalIndex == CommonConstants.NOT_AVAILABLE
  }

  void "fetchGlobalIndexAsStringIfPossible - should work when global index is NOT available - GenericTrackedDomainEventMessage"() {
    given:
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 5, "payload")
    GenericTrackedDomainEventMessage genericTrackedDomainEventMessage = new GenericTrackedDomainEventMessage(null, domainEventMessage)

    when:
    String globalIndex = AxonMessageHelper.fetchGlobalIndexAsStringIfPossible(genericTrackedDomainEventMessage)

    then:
    globalIndex == CommonConstants.NOT_AVAILABLE
  }

  void "fetchAggregateIdentifierIfPossible - should work when aggregateIdentifier is available"() {
    given:
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 5, "payload")

    when:
    String aggregateIdentifier = AxonMessageHelper.fetchAggregateIdentifierIfPossible(domainEventMessage)

    then:
    aggregateIdentifier == "123"
  }

  void "fetchAggregateIdentifierIfPossible - should work when aggregateIdentifier is NOT available"() {
    given:
    GenericMessage genericMessage = new GenericMessage(payload)

    when:
    String aggregateIdentifier = AxonMessageHelper.fetchAggregateIdentifierIfPossible(genericMessage)

    then:
    aggregateIdentifier == CommonConstants.NOT_AVAILABLE

    where:
    payload   | _
    null      | _
    "payload" | _
  }

  void "fetchSequenceNumberAsStringIfPossible - should work when sequence number is available"() {
    given:
    DomainEventMessage domainEventMessage = new GenericDomainEventMessage("MyAggregate", "123", 5, "payload")

    when:
    String sequenceNumber = AxonMessageHelper.fetchSequenceNumberAsStringIfPossible(domainEventMessage)

    then:
    sequenceNumber == "5"
  }

  void "fetchSequenceNumberAsStringIfPossible - should work when sequence number is NOT available"() {
    given:
    GenericMessage genericMessage = new GenericMessage("payload")

    when:
    String sequenceNumber = AxonMessageHelper.fetchSequenceNumberAsStringIfPossible(genericMessage)

    then:
    sequenceNumber == CommonConstants.NOT_AVAILABLE
  }
}
