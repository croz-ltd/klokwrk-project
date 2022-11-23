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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.feature.bookingoffer.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lang.groovy.constant.CommonConstants

@SuppressWarnings("CodeNarc.AbcMetric")
@CompileStatic
class BookingOfferSummaryJpaEntityFactory {
  static BookingOfferSummaryJpaEntity makeBookingOfferSummaryJpaEntity(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)

    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferIdentifier: bookingOfferIdentifier,

        customerIdentifier: bookingOfferCreatedEvent.customer.customerId,
        customerType: bookingOfferCreatedEvent.customer.customerType,

        originLocationUnLoCode: bookingOfferCreatedEvent.routeSpecification.originLocation.unLoCode,
        originLocationName: bookingOfferCreatedEvent.routeSpecification.originLocation.name,
        originLocationCountryName: bookingOfferCreatedEvent.routeSpecification.originLocation.countryName,

        destinationLocationUnLoCode: bookingOfferCreatedEvent.routeSpecification.destinationLocation.unLoCode,
        destinationLocationName: bookingOfferCreatedEvent.routeSpecification.destinationLocation.name,
        destinationLocationCountryName: bookingOfferCreatedEvent.routeSpecification.destinationLocation.countryName,

        departureEarliestTime: bookingOfferCreatedEvent.routeSpecification.departureEarliestTime,
        departureLatestTime: bookingOfferCreatedEvent.routeSpecification.departureLatestTime,
        arrivalLatestTime: bookingOfferCreatedEvent.routeSpecification.arrivalLatestTime,

        commodityTypes: bookingOfferCreatedEvent.commodities*.commodityType.toSet(),
        commodityTotalWeight: bookingOfferCreatedEvent.commodityTotalWeight,
        commodityTotalWeightKg: QuantityFormatter.instance.parse(bookingOfferCreatedEvent.commodityTotalWeight).value,
        commodityTotalContainerTeuCount: bookingOfferCreatedEvent.commodityTotalContainerTeuCount,

        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE,

        firstEventRecordedAt: domainEventMessage.timestamp,
        lastEventRecordedAt: domainEventMessage.timestamp,
        lastEventSequenceNumber: domainEventMessage.sequenceNumber
    )

    return bookingOfferSummaryJpaEntity
  }
}
