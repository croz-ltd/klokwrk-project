/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
