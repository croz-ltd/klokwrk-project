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
import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentSpecification
import org.klokwrk.lang.groovy.misc.InstantUtils

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponseWebContentPayloadAssertion.assertWebResponseContentHasPayloadThat
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
import static org.klokwrk.cargotracker.lib.test.support.web.WebResponseContentMetaDataAssertion.assertWebResponseContentHasMetaDataThat

class BookingFeatureComponentSpecification extends AbstractComponentSpecification {
  @SuppressWarnings("CodeNarc.PropertyName")
  static Integer countOf_createdBookingOffers_forStandardCustomer = 0

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

    assertWebResponseContentHasMetaDataThat(commandResponseContentMap)
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

    assertWebResponseContentHasMetaDataThat(commandResponseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    assertWebResponseContentHasPayloadThat(commandResponseContentMap)
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
    assertWebResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    verifyAll(queryResponseContentMap.payload as Map) {
      size() == 17

      bookingOfferIdentifier == commandResponseBookingOfferIdentifier

      customerType == "STANDARD"

      originLocationUnLoCode == "HRRJK"
      originLocationName == "Rijeka"
      originLocationCountryName == "Croatia"

      destinationLocationUnLoCode == "NLRTM"
      destinationLocationName == "Rotterdam"
      destinationLocationCountryName == "Netherlands"

      Instant.parse(departureEarliestTime as String) == expectedDepartureEarliestTime
      Instant.parse(departureLatestTime as String) == expectedDepartureLatestTime
      Instant.parse(arrivalLatestTime as String) == expectedArrivalLatestTime

      commodityTypes == ["DRY"]
      totalCommodityWeight == [
          value: 1000,
          unitSymbol: "kg"
      ]
      totalContainerTeuCount == 1.00G

      firstEventRecordedAt == lastEventRecordedAt
      Instant.parse(firstEventRecordedAt as String) > currentTime
      Instant.parse(lastEventRecordedAt as String) > currentTime
      lastEventSequenceNumber == 0
    }

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

    assertWebResponseContentHasMetaDataThat(queryResponseContentMap)
        .isViolationOfDomain_notFound()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    // NOTE: For the isEmpty() assertion, we are using the *Assertion class of command here. Although we should use the *Assertion class of a query, it does not really matter.
    //       This way, we are simplifying static imports slightly.
    assertWebResponseContentHasPayloadThat(queryResponseContentMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  void "query - bookingOfferSummary_findAll - should find existing booking offers with default paging and sorting"() {
    given:
    Request queryRequest = makeRequest(makeQueryRequestUrl_bookingOfferSummary_findAll(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findAll_standardCustomer(), "en")

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    queryResponse.statusLine.statusCode == 200

    assertWebResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale("en")

    verifyAll(queryResponseContentMap.payload as Map) {
      size() == 2

      verifyAll(pageInfo as Map, {
        pageOrdinal == 0
        pageElementsCount >= this.countOf_createdBookingOffers_forStandardCustomer
        first == true
        totalElementsCount >= pageElementsCount

        verifyAll(requestedPageRequirement as Map) {
          size() == 2
          ordinal == 0
          size == 25
        }

        verifyAll((it.requestedSortRequirementList as List)[0] as Map) {
          size() == 2
          propertyName == "lastEventRecordedAt"
          direction == "DESC"
        }
      })

      verifyAll(pageContent as List<Map>, {
        size() >= this.countOf_createdBookingOffers_forStandardCustomer
        it.every({ Map pageElement -> pageElement.customerType == "STANDARD" })
        it.every({ Map pageElement -> pageElement.lastEventSequenceNumber == 0 })
      })
    }
  }

  void "query - bookingOfferSummary_searchAll - should find existing booking offers with default paging and sorting"() {
    given:
    Request queryRequest = makeRequest(makeQueryRequestUrl_bookingOfferSummary_searchAll(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_searchAll(), "en")

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Map queryResponseContentMap = new JsonSlurper().parseText(queryResponse.entity.content.text) as Map

    then:
    queryResponse.statusLine.statusCode == 200

    assertWebResponseContentHasMetaDataThat(queryResponseContentMap)
        .isSuccessful()
        .has_general_locale("en")

    verifyAll(queryResponseContentMap.payload as Map) {
      size() == 2

      verifyAll(pageInfo as Map, {
        pageOrdinal == 0
        first == true

        verifyAll(requestedPageRequirement as Map) {
          size() == 2
          ordinal == 0
          size == 25
        }

        verifyAll((it.requestedSortRequirementList as List)[0] as Map) {
          size() == 2
          propertyName == "lastEventRecordedAt"
          direction == "DESC"
        }
      })

      verifyAll(pageContent as List<Map>, {
        size() >= 4
        it.every({ Map pageElement -> pageElement.customerType == "STANDARD" })
        it.every({ Map pageElement -> pageElement.originLocationName == "Rijeka" })
        it.every({ Map pageElement -> pageElement.destinationLocationCountryName == "The United States of America" })
        it.every({ Map pageElement -> pageElement.totalCommodityWeight["value"] >= 15000 && pageElement.totalCommodityWeight["value"] <= 100000 })
        it.every({ Map pageElement -> pageElement.lastEventSequenceNumber == 0 })
      })
    }
  }
}
