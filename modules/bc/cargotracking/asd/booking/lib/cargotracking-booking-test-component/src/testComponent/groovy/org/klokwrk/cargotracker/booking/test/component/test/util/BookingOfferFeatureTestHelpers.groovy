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

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.awaitility.Awaitility
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion
import org.klokwrk.lib.xlang.groovy.base.misc.InstantUtils
import org.testcontainers.containers.GenericContainer

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_base
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_airCooled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_chilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_frozen
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_base
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam

@CompileStatic
class BookingOfferFeatureTestHelpers {
  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(Map queryResponseMap) {
    return BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat(queryResponseMap)
  }

  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(
      Map queryResponseMap,
      @DelegatesTo(value = BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat(queryResponseMap, aClosure)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(
      Map queryResponseMap,
      @DelegatesTo(value = BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseHasPageablePayloadThat(queryResponseMap, aClosure)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(
      Map queryResponseMap,
      @DelegatesTo(value = BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseHasPageablePayloadThat(queryResponseMap, aClosure)
  }

  static BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(Map queryResponseMap) {
    return BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat(queryResponseMap)
  }

  static BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(
      Map queryResponseMap,
      @DelegatesTo(value = BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat(queryResponseMap, aClosure)
  }

  static List<Instant> makeExpectedDepartureAndArrivalInstants(Instant baseTime) {
    return [
        InstantUtils.roundUpInstantToTheHour(baseTime + Duration.ofHours(1)),
        InstantUtils.roundUpInstantToTheHour(baseTime + Duration.ofHours(2)),
        InstantUtils.roundUpInstantToTheHour(baseTime + Duration.ofHours(3))
    ]
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static Map createBookingOffer_succeeded(String commandBody, String acceptLanguage = "en", GenericContainer commandSideApp) {
    return makeRequestAndReturnResponseContentMap_sync(makeCommandRequestUrl_createBookingOffer(commandSideApp), commandBody, acceptLanguage, 200)
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static Map createBookingOffer_failed(String commandBody, String acceptLanguage, GenericContainer commandSideApp) {
    return makeRequestAndReturnResponseContentMap_sync(makeCommandRequestUrl_createBookingOffer(commandSideApp), commandBody, acceptLanguage, 400)
  }

  static Map bookingOfferSummaryFindById_succeeded(String queryBody, String acceptLanguage = "en", GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_async(makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), queryBody, acceptLanguage, 200)
  }

  static Map bookingOfferSummaryFindById_notFound(String queryBody, String acceptLanguage, GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_async(makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), queryBody, acceptLanguage, 404)
  }

  static Map bookingOfferSummaryFindAll_succeeded(String queryBody, String acceptLanguage = "en", GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_sync(makeQueryRequestUrl_bookingOfferSummary_findAll(querySideViewApp), queryBody, acceptLanguage, 200)
  }

  static Map bookingOfferSummarySearchAll_succeeded(String queryBody, String acceptLanguage = "en", GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_sync(makeQueryRequestUrl_bookingOfferSummary_searchAll(querySideViewApp), queryBody, acceptLanguage, 200)
  }

  static Map bookingOfferDetailsFindById_succeeded(String queryBody, String acceptLanguage = "en", GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_async(makeQueryRequestUrl_bookingOfferDetails_findById(querySideViewApp), queryBody, acceptLanguage, 200)
  }

  static Map bookingOfferDetailsFindById_notFound(String queryBody, String acceptLanguage, GenericContainer querySideViewApp) {
    return makeRequestAndReturnResponseContentMap_async(makeQueryRequestUrl_bookingOfferDetails_findById(querySideViewApp), queryBody, acceptLanguage, 404)
  }

  static String makeCommandRequestUrl_createBookingOffer(GenericContainer commandSideApp) {
    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.getMappedPort(8080) }/cargotracking-booking-app-commandside/booking-offer/create-booking-offer"
    return createBookingOfferCommandUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_findById(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracking-booking-app-queryside-view/booking-offer/booking-offer-summary-find-by-id"
    return bookingOfferSummaryQueryUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_findAll(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracking-booking-app-queryside-view/booking-offer/booking-offer-summary-find-all"
    return bookingOfferSummaryQueryUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_searchAll(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracking-booking-app-queryside-view/booking-offer/booking-offer-summary-search-all"
    return bookingOfferSummaryQueryUrl
  }

  static String makeQueryRequestUrl_bookingOfferDetails_findById(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferDetailsQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.getMappedPort(8084) }/cargotracking-booking-app-queryside-view/booking-offer/booking-offer-details-find-by-id"
    return bookingOfferDetailsQueryUrl
  }

  protected static Map makeRequestAndReturnResponseContentMap_sync(String url, String requestBody, String acceptLanguage, Integer expectedResponseStatus) {
    Request request = makeRequest(url, requestBody, acceptLanguage)

    Map responseContentMap = request.execute().handleResponse({ ClassicHttpResponse response ->
      assert response.code == expectedResponseStatus
      return new JsonSlurper().parseText(response.entity.content.text) as Map
    })

    return responseContentMap
  }

  protected static Map makeRequestAndReturnResponseContentMap_async(String url, String requestBody, String acceptLanguage, Integer expectedResponseStatus) {
    Request request = makeRequest(url, requestBody, acceptLanguage)

    Map responseContentMap = null
    Awaitility.await().atMost(Duration.ofSeconds(5)).until({
      Integer queryResponseStatusCode = null

      request.execute().handleResponse({ ClassicHttpResponse response ->
        queryResponseStatusCode = response.code
        if (queryResponseStatusCode == expectedResponseStatus) {
          responseContentMap = new JsonSlurper().parseText(response.entity.content.text) as Map
        }
      })

      queryResponseStatusCode == expectedResponseStatus
    })

    return responseContentMap
  }

  protected static Request makeRequest(String url, String body, String acceptLanguageHeaderValue) {
    Request request = Request.post(url)
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .addHeader("Accept-Charset", "utf-8")
        .addHeader("Accept-Language", acceptLanguageHeaderValue)
        .bodyString(body, ContentType.APPLICATION_JSON)

    return request
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
            .userId("gold-customer@cargotracker.com")
            .routeSpecification(routeSpecificationRequestData_base(currentTime).originLocation("DEHAM").destinationLocation("USLAX"))
            .cargos([cargoRequestData_airCooled().commodityWeight(70_000.kg)])
            .buildAsJsonString(),
        createBookingOfferCommandRequest_base()
            .userId("platinum-customer@cargotracker.com")
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
}
