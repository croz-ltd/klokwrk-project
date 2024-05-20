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

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.lib.xlang.groovy.base.misc.InstantUtils

import java.time.Clock
import java.time.Duration
import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class RouteSpecificationEventDataFixtureBuilder {
  static RouteSpecificationEventDataFixtureBuilder routeSpecification_rijekaToRotterdam(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_rijeka().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_rotterdam().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_rijekaToHamburg(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_rijeka().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_hamburg().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_rijekaToLosAngeles(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_rijeka().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_losAngeles().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_rijekaToNewYork(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_rijeka().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_newYork().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_hamburgToLosAngeles(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_hamburg().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_losAngeles().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_rotterdamToNewYork(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_rotterdam().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_newYork().build())

    return builder
  }

  static RouteSpecificationEventDataFixtureBuilder routeSpecification_hamburgToRotterdam(Clock creationTimeClock = Clock.systemUTC()) {
    RouteSpecificationEventDataFixtureBuilder builder = commonBuilder(creationTimeClock)
        .originLocation(LocationEventDataFixtureBuilder.location_hamburg().build())
        .destinationLocation(LocationEventDataFixtureBuilder.location_rotterdam().build())

    return builder
  }

  private static RouteSpecificationEventDataFixtureBuilder commonBuilder(Clock creationTimeClock) {
    Instant creationTime = Instant.now(creationTimeClock)
    RouteSpecificationEventDataFixtureBuilder builder = new RouteSpecificationEventDataFixtureBuilder()
        .creationTimeClock(creationTimeClock)
        .creationTime(creationTime)
        .departureEarliestTime(creationTime + Duration.ofHours(1))
        .departureLatestTime(creationTime + Duration.ofHours(2))
        .arrivalLatestTime(creationTime + Duration.ofHours(3))

    return builder
  }

  Clock creationTimeClock = Clock.systemUTC()
  Instant creationTime = Instant.now(creationTimeClock)
  Instant departureEarliestTime = creationTime + Duration.ofHours(1)
  Instant departureLatestTime = creationTime + Duration.ofHours(2)
  Instant arrivalLatestTime = creationTime + Duration.ofHours(3)
  LocationEventData originLocation
  LocationEventData destinationLocation

  RouteSpecificationEventData build() {
    RouteSpecificationEventData routeSpecificationEventData = new RouteSpecificationEventData(
        creationTime: creationTime,
        departureEarliestTime: InstantUtils.roundUpInstantToTheHour(departureEarliestTime),
        departureLatestTime: InstantUtils.roundUpInstantToTheHour(departureLatestTime),
        arrivalLatestTime: InstantUtils.roundUpInstantToTheHour(arrivalLatestTime),
        originLocation: originLocation,
        destinationLocation: destinationLocation
    )

    return routeSpecificationEventData
  }
}
