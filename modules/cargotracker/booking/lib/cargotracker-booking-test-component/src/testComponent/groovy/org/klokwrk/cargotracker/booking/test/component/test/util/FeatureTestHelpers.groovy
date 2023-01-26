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
package org.klokwrk.cargotracker.booking.test.component.test.util

import groovy.transform.CompileStatic
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.testcontainers.containers.GenericContainer

import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_base
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_airCooled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_chilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_frozen
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_base
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindByIdQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindByIdQueryRequest_standardCustomer

@CompileStatic
class FeatureTestHelpers {
  static String makeCommandRequestUrl_createBookingOffer(GenericContainer commandSideApp) {
    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.getMappedPort(8080) }/cargotracker-booking-commandside/booking-offer/create-booking-offer"
    return createBookingOfferCommandUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_findById(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracker-booking-queryside-view/booking-offer/booking-offer-summary-find-by-id"
    return bookingOfferSummaryQueryUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_findAll(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracker-booking-queryside-view/booking-offer/booking-offer-summary-find-all"
    return bookingOfferSummaryQueryUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_searchAll(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracker-booking-queryside-view/booking-offer/booking-offer-summary-search-all"
    return bookingOfferSummaryQueryUrl
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  static List<String> makeCommandRequestBodyList_createBookingOffer() {
    Instant currentTime = Instant.now()

    List<String> commandRequestBodyList = [
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_rijekaToRotterdam(currentTime))
            .cargos([cargoRequestData_dry().commodityWeight(30_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("DEHAM"))
            .cargos([cargoRequestData_airCooled().commodityWeight(30_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("USLAX"))
            .cargos([cargoRequestData_chilled().commodityWeight(30_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("USNYC"))
            .cargos([cargoRequestData_frozen().commodityWeight(30_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .userIdentifier("gold-customer@cargotracker.com")
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("DEHAM").destinationLocation("USLAX"))
            .cargos([cargoRequestData_airCooled().commodityWeight(70_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .userIdentifier("platinum-customer@cargotracker.com")
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("NLRTM").destinationLocation("USNYC"))
            .cargos([cargoRequestData_dry().commodityWeight(40_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("DEHAM"))
            .cargos([cargoRequestData_dry()])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("USLAX"))
            .cargos([cargoRequestData_dry().commodityWeight(15_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("HRRJK").destinationLocation("USNYC"))
            .cargos([cargoRequestData_dry().commodityWeight(100_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("DEHAM").destinationLocation("NLRTM"))
            .cargos([cargoRequestData_dry().commodityWeight(15_000.kg)])
            .buildAsJsonString()
    ]

    return commandRequestBodyList
  }

  static String makeQueryRequestBody_bookingOfferSummary_findById(String bookingOfferIdentifier) {
    String queryRequestBody = bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
        .bookingOfferIdentifier(bookingOfferIdentifier)
        .buildAsJsonString()

    return queryRequestBody
  }

  static String makeQueryRequestBody_bookingOfferSummary_findAll_standardCustomer() {
    String queryRequestBody = bookingOfferSummaryFindAllQueryRequest_standardCustomer().buildAsJsonString()
    return queryRequestBody
  }

  static String makeQueryRequestBody_bookingOfferSummary_searchAll() {
    String queryRequestBody = """
      {
        "userIdentifier": "standard-customer@cargotracker.com",
        "customerTypeSearchList": [
          "STANDARD",
          "GOLD"
        ],
        "originLocationName": "Rijeka",
        "destinationLocationCountryName": "The United States",
        "totalCommodityWeightFromIncluding": {
          "value": 15000,
          "unitSymbol": "kg"
        },
        "totalCommodityWeightToIncluding": {
          "value": 100000,
          "unitSymbol": "kg"
        }
      }
      """

    return queryRequestBody
  }

  static Request makeRequest(String url, String body, String acceptLanguageHeaderValue) {
    Request request = Request.Post(url)
                             .addHeader("Content-Type", "application/json")
                             .addHeader("Accept", "application/json")
                             .addHeader("Accept-Charset", "utf-8")
                             .addHeader("Accept-Language", acceptLanguageHeaderValue)
                             .bodyString(body, ContentType.APPLICATION_JSON)

    return request
  }
}
