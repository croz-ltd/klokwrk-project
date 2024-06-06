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
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
import org.springframework.stereotype.Service

@Service
@CompileStatic
class BookingOfferProjectionService {
  private final BookingOfferSummaryProjectionJpaRepository bookingOfferSummaryProjectionJpaRepository
  private final BookingOfferDetailsProjectionJpaRepository bookingOfferDetailsProjectionJpaRepository
  private final ObjectMapper objectMapper
  private final BookingOfferSummaryProjectionHelper bookingOfferSummaryProjectionHelper
  private final BookingOfferDetailsProjectionHelper bookingOfferDetailsProjectionHelper

  BookingOfferProjectionService(
      BookingOfferSummaryProjectionJpaRepository bookingOfferSummaryProjectionJpaRepository,
      BookingOfferDetailsProjectionJpaRepository bookingOfferDetailsProjectionJpaRepository,
      ObjectMapper objectMapper)
  {
    this.bookingOfferSummaryProjectionJpaRepository = bookingOfferSummaryProjectionJpaRepository
    this.bookingOfferDetailsProjectionJpaRepository = bookingOfferDetailsProjectionJpaRepository
    this.objectMapper = objectMapper

    this.bookingOfferSummaryProjectionHelper = new BookingOfferSummaryProjectionHelper(bookingOfferSummaryProjectionJpaRepository)
    this.bookingOfferDetailsProjectionHelper = new BookingOfferDetailsProjectionHelper(bookingOfferDetailsProjectionJpaRepository, objectMapper)
  }

  @EventHandler
  void onBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    bookingOfferSummaryProjectionHelper.storeBookingOfferCreatedEvent(bookingOfferCreatedEvent, domainEventMessage)
    bookingOfferDetailsProjectionHelper.storeBookingOfferCreatedEvent(bookingOfferCreatedEvent, domainEventMessage)
  }

  @EventHandler
  void onRouteSpecificationAddedEvent(RouteSpecificationAddedEvent routeSpecificationAddedEvent, DomainEventMessage domainEventMessage) {
    bookingOfferSummaryProjectionHelper.storeRouteSpecificationAddedEvent(routeSpecificationAddedEvent, domainEventMessage)
    bookingOfferDetailsProjectionHelper.storeRouteSpecificationAddedEvent(routeSpecificationAddedEvent, domainEventMessage)
  }

  @EventHandler
  void onCargoAddedEvent(CargoAddedEvent cargoAddedEvent, DomainEventMessage domainEventMessage) {
    bookingOfferSummaryProjectionHelper.storeCargoAddedEvent(cargoAddedEvent, domainEventMessage)
    bookingOfferDetailsProjectionHelper.storeCargoAddedEvent(cargoAddedEvent, domainEventMessage)
  }
}
