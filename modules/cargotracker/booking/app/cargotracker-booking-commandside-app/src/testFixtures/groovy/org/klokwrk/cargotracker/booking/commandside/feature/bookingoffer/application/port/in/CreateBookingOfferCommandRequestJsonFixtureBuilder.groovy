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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestDataJsonFixtureBuilder
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils

import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CreateBookingOfferCommandRequestJsonFixtureBuilder {
  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam(currentTime))
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry()])

    return jsonFixtureBuilder
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rijekaToRotterdam(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
      .userIdentifier("standard-customer@cargotracker.com")
      .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
      .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam(currentTime))

    return jsonFixtureBuilder
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rotterdamToRijeka(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka(currentTime))

    return jsonFixtureBuilder
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_cargoDry() {
    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry()])

    return jsonFixtureBuilder
  }

  String userIdentifier
  String bookingOfferIdentifier
  RouteSpecificationRequestDataJsonFixtureBuilder routeSpecification
  Collection<CargoRequestDataJsonFixtureBuilder> cargos

  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        userIdentifier: userIdentifier,
        bookingOfferIdentifier: bookingOfferIdentifier,
        routeSpecification: routeSpecification?.buildAsMap(),
        cargos: cargos?.collect({ CargoRequestDataJsonFixtureBuilder cargoRequestDataJsonFixtureBuilder ->
          cargoRequestDataJsonFixtureBuilder.buildAsMap()
        })
    ]

    return mapToReturn
  }
}
