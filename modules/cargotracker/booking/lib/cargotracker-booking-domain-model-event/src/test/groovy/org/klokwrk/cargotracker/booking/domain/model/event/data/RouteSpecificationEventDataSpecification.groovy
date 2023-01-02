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

import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecificationFixtureBuilder
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class RouteSpecificationEventDataSpecification extends Specification {
  static Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))

  void "fromRouteSpecification() should work as expected"() {
    given:
    RouteSpecification routeSpecification = routeSpecificationParam

    when:
    RouteSpecificationEventData routeSpecificationEventData = RouteSpecificationEventData.fromRouteSpecification(routeSpecification)

    then:
    routeSpecificationEventData == routeSpecificationEventDataExpectedParam

    where:
    routeSpecificationParam                                                              | routeSpecificationEventDataExpectedParam
    RouteSpecificationFixtureBuilder.routeSpecification_rijekaToRotterdam(clock).build() | RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam(clock).build()
  }

  void "toRouteSpecification() should work as expected"() {
    given:
    RouteSpecificationEventData routeSpecificationEventData = routeSpecificationEventDataParam

    when:
    RouteSpecification routeSpecification = routeSpecificationEventData.toRouteSpecification()

    then:
    routeSpecification == routeSpecificationExpectedParam

    where:
    routeSpecificationEventDataParam                                                              | routeSpecificationExpectedParam
    RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam(clock).build() | RouteSpecificationFixtureBuilder.routeSpecification_rijekaToRotterdam(clock).build()
  }
}
