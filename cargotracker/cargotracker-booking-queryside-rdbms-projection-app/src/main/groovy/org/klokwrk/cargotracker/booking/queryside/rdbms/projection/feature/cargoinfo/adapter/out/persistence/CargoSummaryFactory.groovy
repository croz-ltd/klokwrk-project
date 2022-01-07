/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargoinfo.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lang.groovy.constant.CommonConstants

@CompileStatic
class CargoSummaryFactory {
  static BookingOfferSummaryJpaEntity createCargoSummaryJpaEntity(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    String bookingOfferIdentifier = bookingOfferCreatedEvent.bookingOfferId.identifier
    String originLocation = bookingOfferCreatedEvent.routeSpecification.originLocation.unLoCode.code
    String destinationLocation = bookingOfferCreatedEvent.routeSpecification.destinationLocation.unLoCode.code
    Long aggregateVersion = domainEventMessage.sequenceNumber

    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferIdentifier: bookingOfferIdentifier, originLocation: originLocation, destinationLocation: destinationLocation,
        aggregateVersion: aggregateVersion,
        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
    )

    return bookingOfferSummaryJpaEntity
  }
}
