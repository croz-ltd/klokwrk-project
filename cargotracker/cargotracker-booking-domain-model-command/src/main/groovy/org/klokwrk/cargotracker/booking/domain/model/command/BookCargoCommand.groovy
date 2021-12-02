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
package org.klokwrk.cargotracker.booking.domain.model.command

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.klokwrk.cargotracker.lib.domain.model.command.BaseCreateCommand
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

@KwrkImmutable
@CompileStatic
class BookCargoCommand implements BaseCreateCommand, PostMapConstructorCheckable {
  CargoId cargoId
  Location originLocation
  Location destinationLocation

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(cargoId, notNullValue())
    requireMatch(originLocation, notNullValue())
    requireMatch(destinationLocation, notNullValue())

    // Since they are pretty closely related to particular use-cases, commands can check for use-case-specific constraints (in contrast with, for example, value objects that are not
    // use-case-specific). Because of this use-case relationship, commands can use classes from boundary-api like CommandException.
    //
    // If the command did not check for use-case-specific constraints, an alternative would be to have those constraints in the application service or the aggregate.
    //
    // If placed in the application service, business logic will be scattered in places where it does not belong. Application service should contain only orchestration logic but not business
    // (validation) rules. It's better if those rules are included in the command model (commands and aggregates).
    //
    // On the other hand, we can have such rules implemented in the aggregate. However, that would require command dispatch and instantiation of the aggregate. Suppose stateless rules do not depend
    // on the aggregate state. In that case, it might be better for performance to check and invoke them before reaching the aggregate (similar as we are doing with validations of application service
    // requests).

    requireKnownLocation(originLocation, "originLocationUnknown")
    requireKnownLocation(destinationLocation, "destinationLocationUnknown")

    requireDifferentOriginAndDestination(originLocation, destinationLocation)
  }

  private void requireKnownLocation(Location location, String violationCodeKey) {
    if (location == Location.UNKNOWN_LOCATION) {
      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  void requireDifferentOriginAndDestination(Location originLocation, Location destinationLocation) {
    if (originLocation == destinationLocation) {
      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("originLocationEqualToDestinationLocation"))
    }
  }

  String getAggregateIdentifier() {
    return cargoId.identifier
  }
}
