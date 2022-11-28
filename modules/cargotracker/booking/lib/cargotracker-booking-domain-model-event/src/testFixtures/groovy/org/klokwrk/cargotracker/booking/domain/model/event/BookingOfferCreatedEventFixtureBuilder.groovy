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
import org.klokwrk.cargotracker.booking.domain.model.event.data.CommodityEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Clock

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferCreatedEventFixtureBuilder {
  /**
   * Creates a builder for default BookingOfferCreatedEvent, which should correspond to the command builder created by
   * {@code CreateBookingOfferCommandFixtureBuilder.createBookingOfferCommand_default()}.
   */
  static BookingOfferCreatedEventFixtureBuilder bookingOfferCreatedEvent_default(Clock currentTimeClock = Clock.systemUTC()) {
    CommodityEventData commodity = CommodityEventDataFixtureBuilder.dry_default().build()

    BookingOfferCreatedEventFixtureBuilder builder = new BookingOfferCreatedEventFixtureBuilder()
        .customer(CustomerEventDataFixtureBuilder.customer_standard().build())
        .bookingOfferId(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString())
        .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam(currentTimeClock).build())
        .commodities([commodity])

    return builder
  }

  CustomerEventData customer
  String bookingOfferId
  RouteSpecificationEventData routeSpecification
  Collection<CommodityEventData> commodities = []

  BookingOfferCreatedEvent build() {
    String commodityTotalWeight
    Quantity<Mass> commodityTotalWeightQuantity = Quantities.getQuantity(0, Units.KILOGRAM)
    commodities.each({ CommodityEventData commodityEventData ->
      Quantity<Mass> commodityWeightQuantity = QuantityFormatter.instance.parse(commodityEventData.commodityWeight) as Quantity<Mass>
      commodityTotalWeightQuantity = commodityTotalWeightQuantity.add(commodityWeightQuantity)
    })
    commodityTotalWeight = QuantityFormatter.instance.format(commodityTotalWeightQuantity.to(Units.KILOGRAM))

    BigDecimal commodityTotalContainerTeuCount = 0
    commodities.each({ CommodityEventData commodityEventData ->
      commodityTotalContainerTeuCount += commodityEventData.containerTeuCount
    })

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: customer,
        bookingOfferId: bookingOfferId,
        routeSpecification: routeSpecification,
        commodities: commodities,
        commodityTotalWeight: commodityTotalWeight,
        commodityTotalContainerTeuCount: commodityTotalContainerTeuCount
    )

    return bookingOfferCreatedEvent
  }
}
