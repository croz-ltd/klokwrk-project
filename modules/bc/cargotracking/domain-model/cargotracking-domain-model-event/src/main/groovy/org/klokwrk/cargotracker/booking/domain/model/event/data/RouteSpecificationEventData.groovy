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
import org.klokwrk.cargotracking.domain.model.value.RouteSpecification
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import java.time.Instant

@KwrkImmutable
@CompileStatic
class RouteSpecificationEventData {
  Instant creationTime
  Instant departureEarliestTime
  Instant departureLatestTime
  Instant arrivalLatestTime
  LocationEventData originLocation
  LocationEventData destinationLocation

  static RouteSpecificationEventData fromRouteSpecification(RouteSpecification routeSpecification) {
    return new RouteSpecificationEventData(
        creationTime: routeSpecification.creationTime,
        departureEarliestTime: routeSpecification.departureEarliestTime,
        departureLatestTime: routeSpecification.departureLatestTime,
        arrivalLatestTime: routeSpecification.arrivalLatestTime,
        originLocation: LocationEventData.fromLocation(routeSpecification.originLocation),
        destinationLocation: LocationEventData.fromLocation(routeSpecification.destinationLocation)
    )
  }

  RouteSpecification toRouteSpecification() {
    return new RouteSpecification(
        creationTime: creationTime,
        departureEarliestTime: departureEarliestTime,
        departureLatestTime: departureLatestTime,
        arrivalLatestTime: arrivalLatestTime,
        originLocation: originLocation.toLocation(),
        destinationLocation: destinationLocation.toLocation()
    )
  }
}
