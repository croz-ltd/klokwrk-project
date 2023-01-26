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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.lib.test.support.fixture.util.JsonFixtureUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class RouteSpecificationRequestDataJsonFixtureBuilder {
  static RouteSpecificationRequestDataJsonFixtureBuilder routeSpecificationRequestData_base(Instant currentTime = Instant.now()) {
    assert currentTime != null

    RouteSpecificationRequestDataJsonFixtureBuilder jsonFixtureBuilder = new RouteSpecificationRequestDataJsonFixtureBuilder()
        .departureEarliestTime(currentTime + Duration.ofHours(1))
        .departureLatestTime(currentTime + Duration.ofHours(2))
        .arrivalLatestTime(currentTime + Duration.ofHours(3))

    return jsonFixtureBuilder
  }

  static RouteSpecificationRequestDataJsonFixtureBuilder routeSpecificationRequestData_rijekaToRotterdam(Instant currentTime = Instant.now()) {
    assert currentTime != null

    RouteSpecificationRequestDataJsonFixtureBuilder jsonFixtureBuilder = new RouteSpecificationRequestDataJsonFixtureBuilder()
        .originLocation("HRRJK")
        .destinationLocation("NLRTM")
        .departureEarliestTime(currentTime + Duration.ofHours(1))
        .departureLatestTime(currentTime + Duration.ofHours(2))
        .arrivalLatestTime(currentTime + Duration.ofHours(3))

    return jsonFixtureBuilder
  }

  static RouteSpecificationRequestDataJsonFixtureBuilder routeSpecificationRequestData_rotterdamToRijeka(Instant currentTime = Instant.now()) {
    assert currentTime != null

    RouteSpecificationRequestDataJsonFixtureBuilder jsonFixtureBuilder = new RouteSpecificationRequestDataJsonFixtureBuilder()
        .originLocation("NLRTM")
        .destinationLocation("HRRJK")
        .departureEarliestTime(currentTime + Duration.ofHours(1))
        .departureLatestTime(currentTime + Duration.ofHours(2))
        .arrivalLatestTime(currentTime + Duration.ofHours(3))

    return jsonFixtureBuilder
  }

  String originLocation
  String destinationLocation

  Instant departureEarliestTime
  Instant departureLatestTime
  Instant arrivalLatestTime

  Instant currentTime

  Map<String, ?> buildAsMap() {
    Map timesToUse = timesToUse(currentTime)

    Map<String, ?> mapToReturn = [
        originLocation: originLocation,
        destinationLocation: destinationLocation,
        departureEarliestTime: timesToUse.departureEarliestTimeToUse,
        departureLatestTime: timesToUse.departureLatestTimeToUse,
        arrivalLatestTime: timesToUse.arrivalEarliestTimeToUse
    ]

    return mapToReturn
  }

  String buildAsJsonString() {
    Map timesToUse = timesToUse(currentTime)

    String stringToReturn = """
        {
            "originLocation": ${ stringToJsonString(originLocation) },
            "destinationLocation": ${ stringToJsonString(destinationLocation) },
            "departureEarliestTime": "$timesToUse.departureEarliestTimeToUse",
            "departureLatestTime": "$timesToUse.departureLatestTimeToUse",
            "arrivalLatestTime": "$timesToUse.arrivalEarliestTimeToUse"
        }
        """

    return stringToReturn
  }

  Map<String, Instant> timesToUse(Instant currentTime) {
    Instant departureEarliestTimeToUse = departureEarliestTime
    Instant departureLatestTimeToUse = departureLatestTime
    Instant arrivalEarliestTimeToUse = arrivalLatestTime

    if (currentTime != null) {
      departureEarliestTimeToUse ?= currentTime + Duration.ofHours(1)
      departureLatestTimeToUse ?= currentTime + Duration.ofHours(2)
      arrivalEarliestTimeToUse ?= currentTime + Duration.ofHours(3)
    }

    Map<String, Instant> timesToUseMap = [
        departureEarliestTimeToUse: departureEarliestTimeToUse,
        departureLatestTimeToUse: departureLatestTimeToUse,
        arrivalEarliestTimeToUse: arrivalEarliestTimeToUse
    ]

    return timesToUseMap
  }
}
