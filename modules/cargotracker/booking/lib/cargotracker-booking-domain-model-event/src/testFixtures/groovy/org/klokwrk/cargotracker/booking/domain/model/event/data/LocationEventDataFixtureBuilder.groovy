/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilityType

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class LocationEventDataFixtureBuilder {
  static LocationEventDataFixtureBuilder location_rijeka() {
    LocationEventDataFixtureBuilder locationEventDataFixtureBuilder = new LocationEventDataFixtureBuilder()
        .name("Rijeka")
        .countryName("Croatia")
        .unLoCode("HRRJK")
        .unLoCodeCoordinates("4520N 01424E")
        .unLoCodeFunction("1234----")
        .portCapabilities(PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities)

    return locationEventDataFixtureBuilder
  }

  static LocationEventDataFixtureBuilder location_rotterdam() {
    LocationEventDataFixtureBuilder locationEventDataFixtureBuilder = new LocationEventDataFixtureBuilder()
        .name("Rotterdam")
        .countryName("Netherlands")
        .unLoCode("NLRTM")
        .unLoCodeCoordinates("5155N 00430E")
        .unLoCodeFunction("12345---")
        .portCapabilities(PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities)

    return locationEventDataFixtureBuilder
  }

  static LocationEventDataFixtureBuilder location_hamburg() {
    LocationEventDataFixtureBuilder locationEventDataFixtureBuilder = new LocationEventDataFixtureBuilder()
        .name("Hamburg")
        .countryName("Germany")
        .unLoCode("DEHAM")
        .unLoCodeCoordinates("5331N 00956E")
        .unLoCodeFunction("12345---")
        .portCapabilities(PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities)

    return locationEventDataFixtureBuilder
  }

  static LocationEventDataFixtureBuilder location_losAngeles() {
    LocationEventDataFixtureBuilder locationEventDataFixtureBuilder = new LocationEventDataFixtureBuilder()
        .name("Los Angeles")
        .countryName("The United States of America")
        .unLoCode("USLAX")
        .unLoCodeCoordinates("3344N 11816W")
        .unLoCodeFunction("1--45---")
        .portCapabilities(PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities)

    return locationEventDataFixtureBuilder
  }

  static LocationEventDataFixtureBuilder location_newYork() {
    LocationEventDataFixtureBuilder locationEventDataFixtureBuilder = new LocationEventDataFixtureBuilder()
        .name("New York")
        .countryName("The United States of America")
        .unLoCode("USNYC")
        .unLoCodeCoordinates("4042N 07400W")
        .unLoCodeFunction("12345---")
        .portCapabilities(PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES.capabilities)

    return locationEventDataFixtureBuilder
  }

  String name
  String countryName
  String unLoCode
  String unLoCodeCoordinates
  String unLoCodeFunction
  Set<PortCapabilityType> portCapabilities

  LocationEventData build() {
    LocationEventData locationEventData = new LocationEventData(
        name: name,
        countryName: countryName,
        unLoCode: unLoCode,
        unLoCodeCoordinates: unLoCodeCoordinates,
        unLoCodeFunction: unLoCodeFunction,
        portCapabilities: portCapabilities
    )

    return locationEventData
  }
}
