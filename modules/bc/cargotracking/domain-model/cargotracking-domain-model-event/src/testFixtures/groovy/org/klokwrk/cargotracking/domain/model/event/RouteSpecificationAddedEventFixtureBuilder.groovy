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
package org.klokwrk.cargotracking.domain.model.event

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils

import java.time.Clock

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class RouteSpecificationAddedEventFixtureBuilder {
  static RouteSpecificationAddedEventFixtureBuilder builder(String bookingOfferId = null) {
    RouteSpecificationAddedEventFixtureBuilder builder = new RouteSpecificationAddedEventFixtureBuilder()

    if (bookingOfferId) {
      builder.bookingOfferId(bookingOfferId)
    }

    return builder
  }

  static RouteSpecificationAddedEventFixtureBuilder routeSpecificationAddedEvent_default(Clock currentTimeClock = Clock.systemUTC()) {
    RouteSpecificationAddedEventFixtureBuilder builder = new RouteSpecificationAddedEventFixtureBuilder()
        .bookingOfferId(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString())
        .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam(currentTimeClock).build())

    return builder
  }

  String bookingOfferId
  RouteSpecificationEventData routeSpecification

  RouteSpecificationAddedEvent build() {
    RouteSpecificationAddedEvent routeSpecificationAddedEvent = new RouteSpecificationAddedEvent(bookingOfferId: bookingOfferId, routeSpecification: routeSpecification)
    return routeSpecificationAddedEvent
  }
}
