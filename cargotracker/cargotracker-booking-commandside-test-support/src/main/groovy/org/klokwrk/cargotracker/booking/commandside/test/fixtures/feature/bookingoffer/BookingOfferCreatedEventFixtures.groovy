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
package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.bookingoffer

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

/**
 * Contains test data fixtures for {@link BookingOfferCreatedEvent}.
 */
@CompileStatic
class BookingOfferCreatedEventFixtures {
  /**
   * Creates valid event where origin and destination locations form supported route.
   */
  static BookingOfferCreatedEvent eventValidRouteSpecification(String cargoIdentifier = UUID.randomUUID()) {
    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtures.commandValidRouteSpecification(cargoIdentifier)
    BookingOfferCreatedEvent bookingOfferCreatedEvent = eventValidForCommand(createBookingOfferCommand)
    return bookingOfferCreatedEvent
  }

  /**
   * Creates event from given command.
   * <p/>
   * Used implementation is the same as when aggregate creates event from command.
   */
  static BookingOfferCreatedEvent eventValidForCommand(CreateBookingOfferCommand createBookingOfferCommand) {
    CommodityInfo commodityInfo = CommodityInfo.make(CommodityType.DRY, 1_000)
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, commodityInfo, Quantities.getQuantity(20_615, Units.KILOGRAM))

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: createBookingOfferCommand.customer,
        bookingOfferId: createBookingOfferCommand.bookingOfferId,
        routeSpecification: createBookingOfferCommand.routeSpecification,
        commodity: commodity,
        bookingTotalCommodityWeight: Quantities.getQuantity(1_000, Units.KILOGRAM),
        bookingTotalContainerTeuCount: 1.00G
    )

    return bookingOfferCreatedEvent
  }
}
