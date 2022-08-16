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
package org.klokwrk.cargotracker.booking.test.component.featurespec

import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.awaitility.Awaitility
import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentSpecification

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestBody_createBookingOffer_chilledCommodity
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestBody_createBookingOffer_dryCommodity
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestBody_createBookingOffer_invalid
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeCommandRequestUrl_createBookingOffer
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestBody_bookingOfferSummary_findById
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeQueryRequestUrl_bookingOfferSummary_findById
import static org.klokwrk.cargotracker.booking.test.component.test.util.FeatureTestHelpers.makeRequest

class BookingFeatureComponentSpecification extends AbstractComponentSpecification {
  void "command - createBookingOffer - should create booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), commandBodyParam as String, acceptLanguageHeaderParam as String)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier

    then:
    commandResponseStatusCode == 200
    commandResponseBookingOfferIdentifier

    where:
    acceptLanguageHeaderParam | commandBodyParam
    "hr-HR"                   | makeCommandRequestBody_createBookingOffer_dryCommodity()
    "hr-HR"                   | makeCommandRequestBody_createBookingOffer_chilledCommodity()
    "en"                      | makeCommandRequestBody_createBookingOffer_dryCommodity()
    "en"                      | makeCommandRequestBody_createBookingOffer_chilledCommodity()
  }

  void "command - createBookingOffer - should not create booking offer for invalid command: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), makeCommandRequestBody_createBookingOffer_invalid(), acceptLanguageHeaderParam as String)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)

    then:
    commandResponseStatusCode == 400
    commandResponseJson.metaData.violation.message == violationMessageParam
    commandResponseJson.payload.isEmpty()

    where:
    acceptLanguageHeaderParam | violationMessageParam
    "hr-HR"                   | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                      | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "query - bookingOfferSummary_findById - should find created booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    Request commandRequest = makeRequest(makeCommandRequestUrl_createBookingOffer(commandSideApp), makeCommandRequestBody_createBookingOffer_dryCommodity(currentTime), "en")
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode
    assert commandResponseStatusCode == 200

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier
    assert commandResponseBookingOfferIdentifier

    when:
    Request queryRequest = makeRequest(
        makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findById(commandResponseBookingOfferIdentifier), acceptLanguageHeaderParam as String
    )

    HttpResponse queryResponse = null
    Awaitility.await().atMost(Duration.ofSeconds(5)).until({
      queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      queryResponseStatusCode == 200
    })

    Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

    then:
    verifyAll(queryResponseJson.payload as Map, {
      size() == 17

      bookingOfferIdentifier == commandResponseBookingOfferIdentifier

      customerType == "STANDARD"

      originLocationUnLoCode == "HRRJK"
      originLocationName == "Rijeka"
      originLocationCountryName == "Croatia"

      destinationLocationUnLoCode == "NLRTM"
      destinationLocationName == "Rotterdam"
      destinationLocationCountryName == "Netherlands"

      Instant.parse(departureEarliestTime as String) >= currentTime + Duration.ofHours(1)
      Instant.parse(departureLatestTime as String) >= currentTime + Duration.ofHours(2)
      Instant.parse(arrivalLatestTime as String) >= currentTime + Duration.ofHours(3)

      commodityTypes == ["DRY"]
      commodityTotalWeightKg == 1000
      commodityTotalContainerTeuCount == 1.00G

      firstEventRecordedAt == lastEventRecordedAt
      Instant.parse(firstEventRecordedAt as String) > currentTime
      Instant.parse(lastEventRecordedAt as String) > currentTime
      lastEventSequenceNumber == 0
    })

    where:
    acceptLanguageHeaderParam | _
    "hr-HR"                   | _
    "en"                      | _
  }

  void "query - bookingOfferSummary_findById - should not find non-existing booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Request queryRequest = makeRequest(
        makeQueryRequestUrl_bookingOfferSummary_findById(querySideViewApp), makeQueryRequestBody_bookingOfferSummary_findById(UUID.randomUUID().toString()), acceptLanguageHeaderParam as String
    )

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
    Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

    then:
    queryResponseStatusCode == 404
    queryResponseJson.payload.isEmpty()
    queryResponseJson.metaData.violation.message == violationMessageParam

    where:
    acceptLanguageHeaderParam | violationMessageParam
    "hr-HR"                   | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                      | "Summary report for specified booking offer is not found."
  }
}
