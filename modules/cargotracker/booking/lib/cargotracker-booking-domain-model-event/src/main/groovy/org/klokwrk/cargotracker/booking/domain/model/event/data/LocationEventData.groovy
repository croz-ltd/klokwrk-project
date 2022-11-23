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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilityType
import org.klokwrk.lang.groovy.transform.KwrkImmutable

@KwrkImmutable
@CompileStatic
class LocationEventData {
  String name
  String countryName
  String unLoCode
  String unLoCodeCoordinates
  String unLoCodeFunction
  Set<PortCapabilityType> portCapabilities

  static LocationEventData fromLocation(Location location) {
    return new LocationEventData(
        name: location.name.name,
        countryName: location.countryName.name,
        unLoCode: location.unLoCode.code,
        unLoCodeCoordinates: location.unLoCodeCoordinates.coordinatesEncoded,
        unLoCodeFunction: location.unLoCodeFunction.functionEncoded,
        portCapabilities: location.portCapabilities.capabilities
    )
  }

  Location toLocation() {
    return Location.make(unLoCode, name, countryName, unLoCodeFunction, unLoCodeCoordinates, new PortCapabilities(capabilities: portCapabilities))
  }
}
