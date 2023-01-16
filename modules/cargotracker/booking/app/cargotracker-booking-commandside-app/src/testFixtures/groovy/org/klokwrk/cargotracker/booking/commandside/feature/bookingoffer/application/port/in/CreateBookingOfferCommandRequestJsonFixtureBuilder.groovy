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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestDataJsonFixtureBuilder
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils

import java.time.Instant

import static org.klokwrk.lang.groovy.misc.JsonUtils.stringToJsonString

@SuppressWarnings("CodeNarc.FactoryMethodName")
@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CreateBookingOfferCommandRequestJsonFixtureBuilder {
  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_base() {
    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam(currentTime))
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry()])

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam(currentTime))
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_chilled()])

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rijekaToRotterdam(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
      .userIdentifier("standard-customer@cargotracker.com")
      .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
      .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam(currentTime))

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_rotterdamToRijeka(Instant currentTime = Instant.now()) {
    assert currentTime != null

    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .routeSpecification(RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka(currentTime))

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_cargoDry() {
    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry()])

    return jsonFixtureBuilder
  }

  static CreateBookingOfferCommandRequestJsonFixtureBuilder createBookingOfferCommandRequest_cargoChilled() {
    CreateBookingOfferCommandRequestJsonFixtureBuilder jsonFixtureBuilder = new CreateBookingOfferCommandRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")
        .bookingOfferIdentifier(CombUuidShortPrefixUtils.makeCombShortPrefix().toString())
        .cargos([CargoRequestDataJsonFixtureBuilder.cargoRequestData_chilled()])

    return jsonFixtureBuilder
  }

  String userIdentifier
  String bookingOfferIdentifier
  RouteSpecificationRequestDataJsonFixtureBuilder routeSpecification
  Collection<CargoRequestDataJsonFixtureBuilder> cargos

  /**
   * Builds a map suitable for converting into JSON (i.e., with Jackson).
   * <p/>
   * Intended to be used from integration tests where appropriately configured Jackson ObjectMapper instance already exists. Therefore, besides integration tests themself, this way we can also
   * indirectly test Jackson's ObjectMapper configuration.
   */
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

  /**
   * Builds a full JSON string.
   * <p/>
   * Intended to be used from tests where JSON mapper is not available, i.e., from component tests with external HTTP client.
   */
  String buildAsJsonString() {
    String stringToReturn = """
        {
            "userIdentifier": ${ stringToJsonString(userIdentifier) },
            "bookingOfferIdentifier": ${ stringToJsonString(bookingOfferIdentifier) },
            "routeSpecification": ${ routeSpecification?.buildAsJsonString() },
            "cargos": ${ cargosToJsonString(cargos) }
        }
        """

    return JsonOutput.prettyPrint(stringToReturn)
  }

  protected String cargosToJsonString(Collection<CargoRequestDataJsonFixtureBuilder> cargos) {
    if (cargos == null) {
      return "null"
    }

    String stringToReturn = """
        [
            ${ cargos.collect({ CargoRequestDataJsonFixtureBuilder builder -> builder.buildAsJsonString() }).join(",") }
        ]
        """

    return stringToReturn
  }
}
