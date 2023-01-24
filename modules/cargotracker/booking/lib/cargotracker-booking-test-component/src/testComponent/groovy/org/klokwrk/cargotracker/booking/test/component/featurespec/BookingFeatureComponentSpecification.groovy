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
package org.klokwrk.cargotracker.booking.test.component.featurespec

import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.awaitility.Awaitility
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponseWebContentPayloadAssertion
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.response.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.response.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentSpecification
import org.klokwrk.lang.groovy.misc.InstantUtils

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestBodyList_createBookingOffer
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestUrl_createBookingOffer
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestBody_bookingOfferSummary_findAll_standardCustomer
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestBody_bookingOfferSummary_findById
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestBody_bookingOfferSummary_searchAll
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestUrl_bookingOfferSummary_findAll
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestUrl_bookingOfferSummary_findById
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestUrl_bookingOfferSummary_searchAll
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeRequest
import static org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentMetaDataAssertion.assertResponseContentHasMetaDataThat

class BookingFeatureComponentSpecification extends AbstractComponentSpecification {
  @SuppressWarnings("CodeNarc.PropertyName")
  static Integer countOf_createdBookingOffers_forStandardCustomer = 0

  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(Map queryResponseContentMap) {
    return BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseContentHasPayloadThat(queryResponseContentMap)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(Map queryResponseContentMap) {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseContentHasPageablePayloadThat(queryResponseContentMap)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(Map queryResponseContentMap) {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseContentHasPageablePayloadThat(queryResponseContentMap)
  }

  void setupSpec() {
    // Execute a list of commands with predefined data used for findAll and searchAll queries
    List<String> commandRequestBodyList = makeCommandRequestBodyList_createBookingOffer()
    List<String> createdBookingOfferIdentifierList = []
    commandRequestBodyList.each { String commandRequestBody ->
      Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), commandRequestBody, "en")
      HttpResponse commandResponse = commandRequest.execute().returnResponse()
      assert commandResponse.statusLine.statusCode == 200

      if (commandRequestBody.contains("standard-customer@cargotracker.com")) {
        countOf_createdBookingOffers_forStandardCustomer++
      }

      Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
      String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier
      createdBookingOfferIdentifierList << commandResponseBookingOfferIdentifier
    }

    // Wait for projection of a event corresponding to the last command
    Request queryRequest = makeRequest(
        makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findById(createdBookingOfferIdentifierList.last()), "en"
    )
    Awaitility.await().atMost(Duration.ofSeconds(5)).until({
      HttpResponse queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      queryResponseStatusCode == 200
    })
  }

  void "command - createBookingOffer - should create booking offer"() {
    given:
    Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), commandBodyParam as String, acceptLanguageParam as String)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Map commandResponseContentMap = new JsonSlurper().parseText(commandResponse.entity.content.text) as Map

    then:
    commandResponse.statusLine.statusCode == 200

    assertResponseContentHasMetaDataThat(commandResponseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    verifyAll(commandResponseContentMap) {
      size() == 2
      metaData

      verifyAll(it.payload as Map) {
        size() == 4
        bookingOfferId
        customer
        routeSpecification
        bookingOfferCargos

        verifyAll(it.bookingOfferId as Map) {
          size() == 1
          identifier
        }
      }
    }

    where:
    acceptLanguageParam | localeStringParam | commandBodyParam
    "hr-HR"             | "hr_HR"           | createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()
    "hr-HR"             | "hr_HR"           | createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString()
    "en"                | "en"              | createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()
    "en"                | "en"              | createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString()
  }

  void "command - createBookingOffer - should not create booking offer for invalid command - invalid destination location"() {
    given:
    Request commandRequest = makeRequest(
        makeCommandRequestUrl_createBookingOffer(commandSideApp),
        createBookingOfferCommandRequest_cargoChilled()
            .routeSpecification(routeSpecificationRequestData_rotterdamToRijeka().destinationLocation("HRZAG"))
            .buildAsJsonString(),
        acceptLanguageParam as String
    )

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Map commandResponseContentMap = new JsonSlurper().parseText(commandResponse.entity.content.text) as Map

    then:
    commandResponse.statusLine.statusCode == 400

    assertResponseContentHasMetaDataThat(commandResponseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    CreateBookingOfferCommandResponseWebContentPayloadAssertion.assertWebResponseContentHasPayloadThat(commandResponseContentMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "query - bookingOfferSummary_findById - should find created booking offer"() {
    given:
    Instant currentTime = Instant.now()
    Instant expectedDepartureEarliestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(1))
    Instant expectedDepartureLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(2))
    Instant expectedArrivalLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(3))

    Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(currentTime).buildAsJsonString(), "en")
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    assert commandResponse.statusLine.statusCode == 200

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier
    assert commandResponseBookingOfferIdentifier

    when:
    Request queryRequest = makeRequest(
        makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findById(commandResponseBookingOfferIdentifier),
        acceptLanguageParam as String
    )

    HttpResponse queryResponse = null
    Awaitility.await().atMost(Duration.ofSeconds(5)).until({
      queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      queryResponseStatusCode == 200
    })

    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    assertResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseContentMap)
        .isSuccessful()
        .hasCustomerTypeOfStandard()
        .hasOriginLocationOfRijeka()
        .hasDestinationLocationOfRotterdam()
        .hasDepartureEarliestTime(expectedDepartureEarliestTime)
        .hasDepartureLatestTime(expectedDepartureLatestTime)
        .hasArrivalLatestTime(expectedArrivalLatestTime)
        .hasCommodityOfDryTypeWithDefaultWeight()
        .hasTotalContainerTeuCount(1.00G)
        .hasEventMetadataOfTheFirstEventWithCorrectTiming(currentTime)

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "query - bookingOfferSummary_findById - should not find non-existing booking offer"() {
    given:
    Request queryRequest = makeRequest(
        makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findById(UUID.randomUUID().toString()), acceptLanguageParam as String
    )

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    queryResponse.statusLine.statusCode == 404

    assertResponseContentHasMetaDataThat(queryResponseContentMap)
        .isViolationOfDomain_notFound()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseContentMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  @SuppressWarnings("UnnecessaryQualifiedReference")
  void "query - bookingOfferSummary_findAll - should find existing booking offers with default paging and sorting"() {
    given:
    Request queryRequest = makeRequest(makeQueryRequestUrl_bookingOfferSummary_findAll(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findAll_standardCustomer(), "en")

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    queryResponse.statusLine.statusCode == 200

    assertResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale("en")

    assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(queryResponseContentMap)
        .isSuccessful()
        .hasPageInfoOfFirstPageWithDefaults()
        .hasPageInfoThat({
          hasPageElementsCountGreaterThenOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
        })
        .hasPageContentSizeGreaterThanOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
        .hasPageContentWithAllElementsThat({
          hasCustomerTypeOfStandard()
          hasEventMetadataOfTheFirstEvent()
        })
  }

  void "query - bookingOfferSummary_searchAll - should find existing booking offers with default paging and sorting"() {
    given:
    Request queryRequest = makeRequest(makeQueryRequestUrl_bookingOfferSummary_searchAll(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_searchAll(), "en")

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    queryResponse.statusLine.statusCode == 200

    assertResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale("en")

    assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(queryResponseContentMap)
        .isSuccessful()
        .hasPageInfoOfFirstPageWithDefaults()
        .hasPageContentSizeGreaterThanOrEqual(4)
        .hasPageContentWithAllElementsThat({
          hasCustomerTypeOfStandard()
          hasOriginLocationOfRijeka()
          hasDestinationLocationCountryName("The United States of America")
          hasTotalCommodityWeightInInclusiveRange(15_000.kg, 100_000.kg)
          hasEventMetadataOfTheFirstEvent()
        })
  }
}
