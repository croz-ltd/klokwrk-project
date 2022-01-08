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
package org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Contains test data fixtures for {@link CreateBookingOfferCommand}.
 */
@CompileStatic
class CreateBookingOfferCommandFixtures {
  static final Map<String, Location> LOCATION_SAMPLE_MAP = [
      "HRKRK": Location.make("HRKRK", "Krk", "Croatia", "1-3-----", "4502N 01435E", PortCapabilities.SEA_PORT_CAPABILITIES),
      "HRRJK": Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRZAG": Location.make("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E", PortCapabilities.NO_PORT_CAPABILITIES),

      "NLRTM": Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
  ]

  /**
   * Creates valid command where origin and destination locations form supported route.
   */
  static CreateBookingOfferCommand commandValidRouteSpecification(String bookingOfferIdentifier = UUID.randomUUID(), Clock clock = Clock.systemUTC()) {
    Instant currentTime = Instant.now(clock)
    Instant currentTimeAndOneHour = currentTime + Duration.ofHours(1)
    Instant currentTimeAndTwoHours = currentTime + Duration.ofHours(2)
    Instant currentTimeAndThreeHours = currentTime + Duration.ofHours(3)

    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        bookingOfferId: BookingOfferId.make(bookingOfferIdentifier),
        routeSpecification: RouteSpecification.create(LOCATION_SAMPLE_MAP.HRRJK, LOCATION_SAMPLE_MAP.NLRTM, currentTimeAndOneHour, currentTimeAndTwoHours, currentTimeAndThreeHours, clock),
        commodityInfo: CommodityInfo.make(CommodityType.DRY, 1000),
        containerDimensionType: ContainerDimensionType.DIMENSION_ISO_22
    )

    return createBookingOfferCommand
  }

  static CreateBookingOfferCommand commandValidCommodityInfo(String bookingOfferIdentifier = UUID.randomUUID(), Clock clock = Clock.systemUTC()) {
    Instant currentTime = Instant.now(clock)
    Instant currentTimeAndOneHour = currentTime + Duration.ofHours(1)
    Instant currentTimeAndTwoHours = currentTime + Duration.ofHours(2)
    Instant currentTimeAndThreeHours = currentTime + Duration.ofHours(3)

    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        bookingOfferId: BookingOfferId.make(bookingOfferIdentifier),
        routeSpecification: RouteSpecification.create(LOCATION_SAMPLE_MAP.HRRJK, LOCATION_SAMPLE_MAP.NLRTM, currentTimeAndOneHour, currentTimeAndTwoHours, currentTimeAndThreeHours, clock),
        commodityInfo: CommodityInfo.make(CommodityType.DRY, 10_000),
        containerDimensionType: ContainerDimensionType.DIMENSION_ISO_22
    )

    return createBookingOfferCommand
  }

  static CreateBookingOfferCommand commandInvalidCommodityInfo(String cargoIdentifier = UUID.randomUUID(), Clock clock = Clock.systemUTC()) {
    Instant currentTime = Instant.now(clock)
    Instant currentTimeAndOneHour = currentTime + Duration.ofHours(1)
    Instant currentTimeAndTwoHours = currentTime + Duration.ofHours(2)
    Instant currentTimeAndThreeHours = currentTime + Duration.ofHours(3)

    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        bookingOfferId: BookingOfferId.make(cargoIdentifier),
        routeSpecification: RouteSpecification.create(LOCATION_SAMPLE_MAP.HRRJK, LOCATION_SAMPLE_MAP.NLRTM, currentTimeAndOneHour, currentTimeAndTwoHours, currentTimeAndThreeHours, clock),
        commodityInfo: CommodityInfo.make(CommodityType.DRY, 5001 * 25_000),
        containerDimensionType: ContainerDimensionType.DIMENSION_ISO_22
    )

    return createBookingOfferCommand
  }
}
