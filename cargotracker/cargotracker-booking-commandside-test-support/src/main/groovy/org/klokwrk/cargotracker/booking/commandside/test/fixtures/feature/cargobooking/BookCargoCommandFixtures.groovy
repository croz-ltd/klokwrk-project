/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.cargotracker.booking.domain.model.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Contains test data fixtures for {@link BookCargoCommand}.
 */
@CompileStatic
class BookCargoCommandFixtures {
  static final Map<String, Location> LOCATION_SAMPLE_MAP = [
      "HRKRK": Location.create("HRKRK", "Krk", "Croatia", "1-3-----", "4502N 01435E", PortCapabilities.SEA_PORT_CAPABILITIES),
      "HRRJK": Location.create("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
      "HRZAG": Location.create("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E", PortCapabilities.NO_PORT_CAPABILITIES),

      "NLRTM": Location.create("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
  ]

  /**
   * Creates valid command where origin and destination locations form supported route.
   */
  static BookCargoCommand commandValidRouteSpecification(String cargoIdentifier = UUID.randomUUID(), Clock clock = Clock.systemUTC()) {
    Instant currentTime = Instant.now(clock)
    Instant currentTimeAndOneHour = currentTime + Duration.ofHours(1)
    Instant currentTimeAndTwoHours = currentTime + Duration.ofHours(2)

    BookCargoCommand bookCargoCommand = new BookCargoCommand(
        cargoId: CargoId.create(cargoIdentifier),
        routeSpecification: RouteSpecification.create(LOCATION_SAMPLE_MAP.HRRJK, LOCATION_SAMPLE_MAP.NLRTM, currentTimeAndOneHour, currentTimeAndTwoHours, clock)
    )

    return bookCargoCommand
  }
}
