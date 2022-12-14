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
package org.klokwrk.cargotracker.booking.domain.model.command

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.command.data.CargoCommandData
import org.klokwrk.cargotracker.booking.domain.model.command.data.CargoCommandDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils

import java.time.Clock

import static org.klokwrk.cargotracker.booking.domain.model.value.CustomerFixtureBuilder.customer_standard
import static org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecificationFixtureBuilder.routeSpecification_rijekaToRotterdam

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CreateBookingOfferCommandFixtureBuilder {
  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CreateBookingOfferCommandFixtureBuilder createBookingOfferCommand_default(Clock currentTimeClock = Clock.systemUTC()) {
    CreateBookingOfferCommandFixtureBuilder builder = new CreateBookingOfferCommandFixtureBuilder()
        .customer(customer_standard().build())
        .bookingOfferId(BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString()))
        .routeSpecification(routeSpecification_rijekaToRotterdam(currentTimeClock).build())
        .cargos([CargoCommandDataFixtureBuilder.createCargoCommandData_default().build()])

    return builder
  }

  Customer customer
  BookingOfferId bookingOfferId
  RouteSpecification routeSpecification
  Collection<CargoCommandData> cargos

  CreateBookingOfferCommand build() {
    CreateBookingOfferCommand command = new CreateBookingOfferCommand(
        customer: customer,
        bookingOfferId: bookingOfferId,
        routeSpecification: routeSpecification,
        cargos: cargos
    )

    return command
  }
}
