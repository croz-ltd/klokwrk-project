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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class LocationFixtureBuilder {
  static LocationFixtureBuilder location_krk() {
    Location location = Location.make("HRKRK", "Krk", "Croatia", "1-3-----", "4502N 01435E", PortCapabilities.SEA_PORT_CAPABILITIES)
    LocationFixtureBuilder locationFixtures = makeFromLocation(location)
    return locationFixtures
  }

  static LocationFixtureBuilder location_rijeka() {
    Location location = Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES)
    LocationFixtureBuilder locationFixtures = makeFromLocation(location)
    return locationFixtures
  }

  static LocationFixtureBuilder location_zagreb() {
    Location location = Location.make("HRZAG", "Zagreb", "Croatia", "-2345---", "4548N 01600E", PortCapabilities.NO_PORT_CAPABILITIES)
    LocationFixtureBuilder locationFixtures = makeFromLocation(location)
    return locationFixtures
  }

  static LocationFixtureBuilder location_rotterdam() {
    Location location = Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES)
    LocationFixtureBuilder locationFixtures = makeFromLocation(location)
    return locationFixtures
  }

  private static LocationFixtureBuilder makeFromLocation(Location location) {
    LocationFixtureBuilder locationFixtures = new LocationFixtureBuilder()
        .unLoCode(location.unLoCode)
        .name(location.name)
        .countryName(location.countryName)
        .unLoCodeFunction(location.unLoCodeFunction)
        .unLoCodeCoordinates(location.unLoCodeCoordinates)
        .portCapabilities(location.portCapabilities)

    return locationFixtures
  }

  UnLoCode unLoCode
  InternationalizedName name
  InternationalizedName countryName
  UnLoCodeFunction unLoCodeFunction
  UnLoCodeCoordinates unLoCodeCoordinates
  PortCapabilities portCapabilities

  Location build() {
    Location location = new Location(
        unLoCode: unLoCode, name: name, countryName: countryName, unLoCodeFunction: unLoCodeFunction, unLoCodeCoordinates: unLoCodeCoordinates, portCapabilities: portCapabilities
    )

    return location
  }
}
