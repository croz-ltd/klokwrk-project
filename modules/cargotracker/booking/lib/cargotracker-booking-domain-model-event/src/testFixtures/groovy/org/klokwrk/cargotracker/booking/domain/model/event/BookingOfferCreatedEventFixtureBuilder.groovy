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
package org.klokwrk.cargotracker.booking.domain.model.event

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.event.data.CommodityEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Clock

import static org.klokwrk.cargotracker.booking.domain.model.value.CustomerFixtureBuilder.customer_standard
import static org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecificationFixtureBuilder.routeSpecification_rijekaToRotterdam

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferCreatedEventFixtureBuilder {
  /**
   * Creates a builder for default BookingOfferCreatedEvent, which should correspond to the command builder created by
   * {@code CreateBookingOfferCommandFixtureBuilder.createBookingOfferCommand_default()}.
   */
  static BookingOfferCreatedEventFixtureBuilder bookingOfferCreatedEvent_default(Clock currentTimeClock = Clock.systemUTC()) {
    CommodityInfo commodityInfo = CommodityInfo.make(CommodityType.DRY, 1_000)
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, commodityInfo, Quantities.getQuantity(20_615, Units.KILOGRAM))

    BookingOfferCreatedEventFixtureBuilder builder = new BookingOfferCreatedEventFixtureBuilder()
        .customer(customer_standard().build())
        .bookingOfferId(BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString()))
        .routeSpecification(routeSpecification_rijekaToRotterdam(currentTimeClock).build())
        .commodities([commodity])
        .bookingTotalCommodityWeight(Quantities.getQuantity(1_000, Units.KILOGRAM))
        .bookingTotalContainerTeuCount(1.00G)

    return builder
  }

  Customer customer
  BookingOfferId bookingOfferId
  RouteSpecification routeSpecification
  Collection<Commodity> commodities
  Quantity<Mass> bookingTotalCommodityWeight
  BigDecimal bookingTotalContainerTeuCount

  BookingOfferCreatedEvent build() {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: CustomerEventData.fromCustomer(customer),
        bookingOfferId: bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(routeSpecification),
        commodities: commodities.collect({ Commodity commodity -> CommodityEventData.fromCommodity(commodity) }),
        commodityTotalWeight: QuantityFormatter.instance.format(bookingTotalCommodityWeight),
        commodityTotalContainerTeuCount: bookingTotalContainerTeuCount
    )

    return bookingOfferCreatedEvent
  }
}
