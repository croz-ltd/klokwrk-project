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

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa.BookingOfferDetailsJpaEntity
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants

@CompileStatic
class BookingOfferDetailsProjectionHelper {
  private final BookingOfferDetailsProjectionJpaRepository bookingOfferDetailsProjectionJpaRepository
  private final ObjectMapper objectMapper

  BookingOfferDetailsProjectionHelper(BookingOfferDetailsProjectionJpaRepository bookingOfferDetailsProjectionJpaRepository, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper
    this.bookingOfferDetailsProjectionJpaRepository = bookingOfferDetailsProjectionJpaRepository
  }

  void storeBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
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

    bookingOfferDetailsProjectionJpaRepository.persist(bookingOfferDetailsJpaEntity)
  }

  void storeRouteSpecificationAddedEvent(RouteSpecificationAddedEvent routeSpecificationAddedEvent, DomainEventMessage domainEventMessage) {
    BookingOfferDetailsJpaEntity bookingOfferDetailsJpaEntity = bookingOfferDetailsProjectionJpaRepository.findById(UUID.fromString(routeSpecificationAddedEvent.bookingOfferId)).get()
    Map<String, ?> detailsMapToUpdate = objectMapper.readValue(bookingOfferDetailsJpaEntity.details, Map)
    detailsMapToUpdate.routeSpecification = routeSpecificationAddedEvent.routeSpecification.propertiesFiltered
    bookingOfferDetailsJpaEntity.with {
      details = objectMapper.writeValueAsString(detailsMapToUpdate)

      inboundChannelName = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE
      inboundChannelType = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
      lastEventRecordedAt = domainEventMessage.timestamp
      lastEventSequenceNumber = domainEventMessage.sequenceNumber
    }
  }

  void storeCargoAddedEvent(CargoAddedEvent cargoAddedEvent, DomainEventMessage domainEventMessage) {
    BookingOfferDetailsJpaEntity bookingOfferDetailsJpaEntity = bookingOfferDetailsProjectionJpaRepository.findById(UUID.fromString(cargoAddedEvent.bookingOfferId)).get()
    Map<String, ?> detailsMapToUpdate = objectMapper.readValue(bookingOfferDetailsJpaEntity.details, Map)

    detailsMapToUpdate.cargos ?= []
    (detailsMapToUpdate.cargos as List).add(cargoAddedEvent.cargo.propertiesFiltered)

    detailsMapToUpdate.totalCommodityWeight = cargoAddedEvent.totalCommodityWeight
    detailsMapToUpdate.totalContainerTeuCount = cargoAddedEvent.totalContainerTeuCount

    bookingOfferDetailsJpaEntity.with {
      details = objectMapper.writeValueAsString(detailsMapToUpdate)

      inboundChannelName = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE
      inboundChannelType = domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
      lastEventRecordedAt = domainEventMessage.timestamp
      lastEventSequenceNumber = domainEventMessage.sequenceNumber
    }
  }
}
