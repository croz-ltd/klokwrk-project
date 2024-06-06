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
package org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.feature.bookingoffer.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa.BookingOfferSummaryJpaEntity
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import tech.units.indriya.unit.Units

@CompileStatic
class BookingOfferSummaryProjectionHelper {
  private final BookingOfferSummaryProjectionJpaRepository bookingOfferSummaryProjectionJpaRepository

  BookingOfferSummaryProjectionHelper(BookingOfferSummaryProjectionJpaRepository bookingOfferSummaryProjectionJpaRepository) {
    this.bookingOfferSummaryProjectionJpaRepository = bookingOfferSummaryProjectionJpaRepository
  }

  void storeBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    UUID bookingOfferId = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)

    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferId: bookingOfferId,

        customerId: bookingOfferCreatedEvent.customer.customerId,
        customerType: bookingOfferCreatedEvent.customer.customerType,

        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE,

        firstEventRecordedAt: domainEventMessage.timestamp,
        lastEventRecordedAt: domainEventMessage.timestamp,
        lastEventSequenceNumber: domainEventMessage.sequenceNumber
    )

    bookingOfferSummaryProjectionJpaRepository.persist(bookingOfferSummaryJpaEntity)
  }

  void storeRouteSpecificationAddedEvent(RouteSpecificationAddedEvent routeSpecificationAddedEvent, DomainEventMessage domainEventMessage) {
    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = bookingOfferSummaryProjectionJpaRepository.findById(UUID.fromString(routeSpecificationAddedEvent.bookingOfferId)).get()
    bookingOfferSummaryJpaEntity.with {
      originLocationUnLoCode = routeSpecificationAddedEvent.routeSpecification.originLocation.unLoCode
      originLocationName = routeSpecificationAddedEvent.routeSpecification.originLocation.name
      originLocationCountryName = routeSpecificationAddedEvent.routeSpecification.originLocation.countryName

      destinationLocationUnLoCode = routeSpecificationAddedEvent.routeSpecification.destinationLocation.unLoCode
      destinationLocationName = routeSpecificationAddedEvent.routeSpecification.destinationLocation.name
      destinationLocationCountryName = routeSpecificationAddedEvent.routeSpecification.destinationLocation.countryName

      departureEarliestTime = routeSpecificationAddedEvent.routeSpecification.departureEarliestTime
      departureLatestTime = routeSpecificationAddedEvent.routeSpecification.departureLatestTime
      arrivalLatestTime = routeSpecificationAddedEvent.routeSpecification.arrivalLatestTime

      inboundChannelName = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE
      inboundChannelType = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
      lastEventRecordedAt = domainEventMessage.timestamp
      lastEventSequenceNumber = domainEventMessage.sequenceNumber
    }
  }

  void storeCargoAddedEvent(CargoAddedEvent cargoAddedEvent, DomainEventMessage domainEventMessage) {
    BookingOfferSummaryJpaEntity bookingOfferSummaryJpaEntity = bookingOfferSummaryProjectionJpaRepository.findByIdWithCommodityTypes(UUID.fromString(cargoAddedEvent.bookingOfferId)).get()
    bookingOfferSummaryJpaEntity.with {
      commodityTypes ?= [] as Set<CommodityType>
      commodityTypes.add(cargoAddedEvent.cargo.commodityType)

      totalCommodityWeightKg = cargoAddedEvent.totalCommodityWeight.to(Units.KILOGRAM).value.toLong()
      totalCommodityWeight = cargoAddedEvent.totalCommodityWeight.format()
      totalContainerTeuCount = cargoAddedEvent.totalContainerTeuCount

      inboundChannelName = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE
      inboundChannelType = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
      lastEventRecordedAt = domainEventMessage.timestamp
      lastEventSequenceNumber = domainEventMessage.sequenceNumber
    }
  }
}
