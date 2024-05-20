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
package org.klokwrk.cargotracking.domain.model.command

import groovy.transform.CompileStatic
import groovy.transform.Generated
import org.klokwrk.cargotracking.domain.model.command.data.CargoCommandData
import org.klokwrk.cargotracking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.Location
import org.klokwrk.cargotracking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.cargotracking.lib.domain.model.command.BaseCreateCommand
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

@KwrkImmutable
@CompileStatic
class CreateBookingOfferCommand implements BaseCreateCommand, PostMapConstructorCheckable {
  Customer customer
  BookingOfferId bookingOfferId
  RouteSpecification routeSpecification
  Collection<CargoCommandData> cargos

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    // Here we are comply to the validation ordering as explained in ADR-0013.
    requireMatch(customer, notNullValue())
    requireMatch(bookingOfferId, notNullValue())
    requireMatch(routeSpecification, notNullValue())
    requireMatch(cargos, notNullValue())
    requireMatch(cargos, not(empty()))
    requireMatch(cargos, everyItem(instanceOf(CargoCommandData)))

    // Since they are pretty closely related to particular use-cases, commands can check for use-case-specific constraints (in contrast with, for example, value objects that are not
    // use-case-specific).
    //
    // If the command did not check for use-case-specific constraints, an alternative would be to have those constraints in the application service or the aggregate.
    //
    // If placed in the application service, business logic will be scattered in places where it does not belong. Application service should contain only orchestration logic but not business
    // (validation) rules. It's better if those rules are included in the command model (commands and aggregates).
    //
    // On the other hand, we can have such rules implemented in the aggregate. However, that would require command dispatch and instantiation of the aggregate. Suppose stateless rules do not depend
    // on the aggregate state. In that case, it might be better for performance to check and invoke them before reaching the aggregate (similar as we are doing with validations of application service
    // requests).
    //
    // After all this discussion, it is worth mentioning that constraints are not really use-case specific in our concrete example here, and we can safely move them into the RouteSpecification value
    // object. In fact, that concrete constraint is repeated in RouteSpecification, therefore the same constraint in command will never be violated as RouteSpecification is already constructed.
    // Still, we'll also leave constraint here for illustration purposes.

    requireKnownLocation(routeSpecification.originLocation, "createBookingOfferCommand.originLocationUnknown")
    requireKnownLocation(routeSpecification.destinationLocation, "createBookingOfferCommand.destinationLocationUnknown")
  }

  @Generated // To avoid unnecessary drop-down in code coverage (see the last paragraph in comment inside postMapConstructorCheck() method).
  private void requireKnownLocation(Location location, String violationCodeKey) {
    if (location == Location.UNKNOWN_LOCATION) {
      throw new CommandException(ViolationInfo.makeForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  String getAggregateIdentifier() {
    return bookingOfferId.identifier
  }
}
