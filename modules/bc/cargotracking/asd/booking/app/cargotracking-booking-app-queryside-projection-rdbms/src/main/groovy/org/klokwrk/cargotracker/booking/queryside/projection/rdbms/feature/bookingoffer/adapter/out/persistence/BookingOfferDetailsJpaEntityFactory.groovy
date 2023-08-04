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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.feature.bookingoffer.adapter.out.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferDetailsJpaEntity
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants

@CompileStatic
class BookingOfferDetailsJpaEntityFactory {
  static BookingOfferDetailsJpaEntity makeBookingOfferDetailsJpaEntity(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage, ObjectMapper objectMapper) {
    UUID bookingOfferId = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String details = objectMapper.writeValueAsString(bookingOfferCreatedEvent)
    String customerId = bookingOfferCreatedEvent.customer.customerId

    BookingOfferDetailsJpaEntity bookingOfferDetailsJpaEntity = new BookingOfferDetailsJpaEntity(
        bookingOfferId: bookingOfferId,
        customerId: customerId,

        details: details,

        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE,

        firstEventRecordedAt: domainEventMessage.timestamp,
        lastEventRecordedAt: domainEventMessage.timestamp,
        lastEventSequenceNumber: domainEventMessage.sequenceNumber
    )

    return bookingOfferDetailsJpaEntity
  }
}
