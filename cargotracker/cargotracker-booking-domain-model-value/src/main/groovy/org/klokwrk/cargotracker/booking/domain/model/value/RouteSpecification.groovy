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
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.sameInstance

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
  }

  Boolean canDestinationAcceptCargoFromOrigin() {
    // We can question here why equality check is not executed during construction.
    //
    // The first reason is technical and dependent on the current implementation. The canDestinationAcceptCargoFromOrigin() method is executed by business logic inside the aggregate. And in the
    // aggregate, we want to throw CommandException instead of AssertionError as throwing CommandException (or DomainException) directly from the value object is not appropriate.
    //
    // One alternative would be to create a custom exception like InvalidParameterException and place it in the domain-model-value module. InvalidParameterException should then carry some type of
    // error code to translate it in the desired domain exception.
    //
    // There is a second reason, though. We can use the RouteSpecification value object in different scenarios. In some of them, all checks from canDestinationAcceptCargoFromOrigin() would not be
    // necessary (i.e., when fetching RouteSpecification from the database. We might even conclude that we need another type of object, perhaps something like RouteSpecificationPolicy domain service,
    // which will be responsible for checking all required business rules for a particular context.
    //
    // Anyway, while value objects can contain helpful domain-related checks and methods, they should be at a reasonably low level as we can use the same value objects in quite different use cases.
    //
    // At the moment, we have here a simple static method implementing some more advanced business rules. However, we can move it later outside of this class to a more appropriate place.

    if (originLocation == destinationLocation) {
      return false
    }

    if (destinationLocation.unLoCodeFunction.isPort() && originLocation.unLoCodeFunction.isPort()) {
      return true
    }

    if (destinationLocation.unLoCodeFunction.isRailTerminal() && originLocation.unLoCodeFunction.isRailTerminal()) {
      return true
    }

    return false
  }
}
