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

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import org.klokwrk.lib.lo.uom.format.KwrkQuantityFormat

@SuppressWarnings("CodeNarc.AbcMetric")
@CompileStatic
class BookingOfferSummaryJpaEntityFactory {
  static BookingOfferSummaryJpaEntity makeBookingOfferSummaryJpaEntity(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    UUID bookingOfferId = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)

    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferId: bookingOfferId,

        customerId: bookingOfferCreatedEvent.customer.customerId,
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

        commodityTypes: bookingOfferCreatedEvent.cargos*.commodityType.toSet(),
        totalCommodityWeight: KwrkQuantityFormat.instance.format(bookingOfferCreatedEvent.totalCommodityWeight),
        totalCommodityWeightKg: bookingOfferCreatedEvent.totalCommodityWeight.value,
        totalContainerTeuCount: bookingOfferCreatedEvent.totalContainerTeuCount,

        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE,

        firstEventRecordedAt: domainEventMessage.timestamp,
        lastEventRecordedAt: domainEventMessage.timestamp,
        lastEventSequenceNumber: domainEventMessage.sequenceNumber
    )

    return bookingOfferSummaryJpaEntity
  }
}
