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
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder
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
    CargoEventData cargo = CargoEventDataFixtureBuilder.cargo_dry().build()

    BookingOfferCreatedEventFixtureBuilder builder = new BookingOfferCreatedEventFixtureBuilder()
        .customer(CustomerEventDataFixtureBuilder.customer_standard().build())
        .bookingOfferId(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString())
        .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam(currentTimeClock).build())
        .cargos([cargo])

    return builder
  }

  CustomerEventData customer
  String bookingOfferId
  RouteSpecificationEventData routeSpecification
  Collection<CargoEventData> cargos = []

  BookingOfferCreatedEvent build() {
    Quantity<Mass> totalCommodityWeightQuantity = Quantities.getQuantity(0, Units.KILOGRAM)
    cargos.each({ CargoEventData cargoEventData ->
      Quantity<Mass> commodityWeightQuantity = cargoEventData.commodityWeight
      totalCommodityWeightQuantity = totalCommodityWeightQuantity.add(commodityWeightQuantity)
    })
    Quantity<Mass> totalCommodityWeight = totalCommodityWeightQuantity.to(Units.KILOGRAM)

    BigDecimal totalContainerTeuCount = 0
    cargos.each({ CargoEventData cargoEventData ->
      totalContainerTeuCount += cargoEventData.containerTeuCount
    })

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: customer,
        bookingOfferId: bookingOfferId,
        routeSpecification: routeSpecification,
        cargos: cargos,
        totalCommodityWeight: totalCommodityWeight,
        totalContainerTeuCount: totalContainerTeuCount
    )

    return bookingOfferCreatedEvent
  }
}
