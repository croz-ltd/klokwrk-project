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

import java.time.Clock
import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.domain.model.value.LocationFixtureBuilder.location_rijeka
import static org.klokwrk.cargotracker.booking.domain.model.value.LocationFixtureBuilder.location_rotterdam

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class RouteSpecificationFixtureBuilder {
  static RouteSpecificationFixtureBuilder routeSpecification_rijekaToRotterdam(Clock creationTimeClock = Clock.systemUTC()) {
    Instant creationTime = Instant.now(creationTimeClock)
    RouteSpecificationFixtureBuilder builder = new RouteSpecificationFixtureBuilder()
        .originLocation(location_rijeka().build())
        .destinationLocation(location_rotterdam().build())
        .departureEarliestTime(creationTime + Duration.ofHours(1))
        .departureLatestTime(creationTime + Duration.ofHours(2))
        .arrivalLatestTime(creationTime + Duration.ofHours(3))
        .creationTimeClock(creationTimeClock)

    return builder
  }

  Clock creationTimeClock = Clock.systemUTC()
  Instant creationTime = Instant.now(creationTimeClock)
  Location originLocation
  Location destinationLocation
  Instant departureEarliestTime = creationTime + Duration.ofHours(1)
  Instant departureLatestTime = creationTime + Duration.ofHours(2)
  Instant arrivalLatestTime = creationTime + Duration.ofHours(3)

  RouteSpecification build() {
    RouteSpecification routeSpecification = RouteSpecification.make(originLocation, destinationLocation, departureEarliestTime, departureLatestTime, arrivalLatestTime, creationTimeClock)
    return routeSpecification
  }
}
