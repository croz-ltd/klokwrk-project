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
package org.klokwrk.cargotracking.domain.model.event.data

import org.klokwrk.cargotracking.domain.model.value.Location
import org.klokwrk.cargotracking.domain.model.value.LocationFixtureBuilder
import spock.lang.Specification

class LocationEventDataSpecification extends Specification {
  void "fromLocation() should work as expected"() {
    given:
    Location location = locationParam

    when:
    LocationEventData locationEventData = LocationEventData.fromLocation(location)

    then:
    locationEventData == locationEventDataExpectedParam

    where:
    locationParam                                        | locationEventDataExpectedParam
    LocationFixtureBuilder.location_rijeka().build()     | LocationEventDataFixtureBuilder.location_rijeka().build()
    LocationFixtureBuilder.location_rotterdam().build()  | LocationEventDataFixtureBuilder.location_rotterdam().build()
    LocationFixtureBuilder.location_hamburg().build()    | LocationEventDataFixtureBuilder.location_hamburg().build()
    LocationFixtureBuilder.location_losAngeles().build() | LocationEventDataFixtureBuilder.location_losAngeles().build()
    LocationFixtureBuilder.location_newYork().build()    | LocationEventDataFixtureBuilder.location_newYork().build()
  }

  void "toLocation() should work as expected"() {
    given:
    LocationEventData locationEventData = locationEventDataParam

    when:
    Location location = locationEventData.toLocation()

    then:
    location == locationExpectedParam

    where:
    locationEventDataParam                                        | locationExpectedParam
    LocationEventDataFixtureBuilder.location_rijeka().build()     | LocationFixtureBuilder.location_rijeka().build()
    LocationEventDataFixtureBuilder.location_rotterdam().build()  | LocationFixtureBuilder.location_rotterdam().build()
    LocationEventDataFixtureBuilder.location_hamburg().build()    | LocationFixtureBuilder.location_hamburg().build()
    LocationEventDataFixtureBuilder.location_losAngeles().build() | LocationFixtureBuilder.location_losAngeles().build()
    LocationEventDataFixtureBuilder.location_newYork().build()    | LocationFixtureBuilder.location_newYork().build()
  }
}
