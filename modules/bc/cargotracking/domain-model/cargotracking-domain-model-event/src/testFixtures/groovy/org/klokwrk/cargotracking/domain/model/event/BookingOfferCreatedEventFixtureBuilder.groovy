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
package org.klokwrk.cargotracking.domain.model.event

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracking.domain.model.event.data.CustomerEventDataFixtureBuilder
import org.klokwrk.cargotracking.lib.domain.model.event.BaseEvent
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils

import java.time.Clock

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferCreatedEventFixtureBuilder {
  /**
   * Creates a builder for default BookingOfferCreatedEvent, which should correspond to the command builder created by
   * {@code CreateBookingOfferCommandFixtureBuilder.createBookingOfferCommand_default()}.
   */
  static BookingOfferCreatedEventFixtureBuilder bookingOfferCreatedEvent_default(Clock currentTimeClock = Clock.systemUTC()) {
    BookingOfferCreatedEventFixtureBuilder builder = new BookingOfferCreatedEventFixtureBuilder()
        .bookingOfferId(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString())
        .customer(CustomerEventDataFixtureBuilder.customer_standard().build())

    return builder
  }

  @SuppressWarnings("CodeNarc.UnnecessaryCast")
  static List<BaseEvent> bookingOfferCreation_complete_defaultEventsSequence(Clock currentTimeClock = Clock.systemUTC()) {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = bookingOfferCreatedEvent_default(currentTimeClock).build()
    String bookingOfferId = bookingOfferCreatedEvent.bookingOfferId

    RouteSpecificationAddedEvent routeSpecificationAddedEvent = RouteSpecificationAddedEventFixtureBuilder.routeSpecificationAddedEvent_default().bookingOfferId(bookingOfferId).build()
    CargoAddedEvent cargoAddedEvent = CargoAddedEventFixtureBuilder.cargoAddedEvent_default().bookingOfferId(bookingOfferId).build()

    return [bookingOfferCreatedEvent, routeSpecificationAddedEvent, cargoAddedEvent] as List<BaseEvent>
  }

  String bookingOfferId
  CustomerEventData customer

  BookingOfferCreatedEvent build() {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(bookingOfferId: bookingOfferId, customer: customer)
    return bookingOfferCreatedEvent
  }
}
