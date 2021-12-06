/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Represents route specification.
 * <p/>
 * Contains data which is core input for searching suitable and available routes for a cargo.
 */
@KwrkImmutable
@CompileStatic
class RouteSpecification implements PostMapConstructorCheckable {
  Location originLocation
  Location destinationLocation

  static RouteSpecification create(Location originLocation, Location destinationLocation) {
    RouteSpecification createdRouteSpecification = new RouteSpecification(originLocation: originLocation, destinationLocation: destinationLocation)
    return createdRouteSpecification
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(originLocation, notNullValue())
    requireMatch(destinationLocation, notNullValue())

    requireKnownLocation(originLocation, "routeSpecification.unknownOriginLocation")
    requireKnownLocation(destinationLocation, "routeSpecification.unknownDestinationLocation")
    requireDifferentOriginAndDestination(originLocation, destinationLocation, "routeSpecification.originAndDestinationLocationAreEqual")
    requireCanRouteCargoFromOriginToDestination(originLocation, destinationLocation, "routeSpecification.cannotRouteCargoFromOriginToDestination")
  }

  private void requireKnownLocation(Location location, String violationCodeKey) {
    if (location == Location.UNKNOWN_LOCATION) {
      throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  private void requireDifferentOriginAndDestination(Location originLocation, Location destinationLocation, String violationCodeKey) {
    if (originLocation == destinationLocation) {
      throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  private void requireCanRouteCargoFromOriginToDestination(Location originLocation, Location destinationLocation, String violationCodeKey) {
    if (!(originLocation.portCapabilities.isSeaContainerPort() && destinationLocation.portCapabilities.isSeaContainerPort())) {
      throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }
}
