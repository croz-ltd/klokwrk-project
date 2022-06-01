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
import org.apache.http.entity.ContentType
import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentIntegrationSpecification
import spock.util.concurrent.PollingConditions

import java.time.Duration
import java.time.Instant

class BookingComponentFeatureSpecification extends AbstractComponentIntegrationSpecification {
  void "command - should create booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/booking-offer/create-booking-offer"
    String commandPostRequestBody = """
        {
          "userIdentifier": "standard-customer@cargotracker.com",
          "routeSpecification": {
            "originLocation": "HRRJK",
            "destinationLocation": "NLRTM",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }",
            "arrivalLatestTime": "${ arrivalLatestTime }"
          },
          "commodityInfo": {
            "commodityType": "chilled",
            "totalWeightInKilograms": 1000,
            "requestedStorageTemperatureInCelsius": 5
          },
          "containerDimensionType": "DIMENSION_ISO_22"
        }
        """

    Request commandRequest = Request.Post(createBookingOfferCommandUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier

    then:
    commandResponseStatusCode == 200
    commandResponseBookingOfferIdentifier

    where:
    acceptLanguageHeaderParam | _
    "hr-HR"                   | _
    "en"                      | _
  }

  void "query - should find created booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/booking-offer/create-booking-offer"
    String commandPostRequestBody = """
        {
          "userIdentifier": "standard-customer@cargotracker.com",
          "routeSpecification": {
            "originLocation": "HRRJK",
            "destinationLocation": "NLRTM",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }",
            "arrivalLatestTime": "${ arrivalLatestTime }"
          },
          "commodityInfo": {
            "commodityType": "dry",
            "totalWeightInKilograms": 1000
          },
          "containerDimensionType": "DIMENSION_ISO_22"
        }
        """

    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideApp.host }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/booking-offer/booking-offer-summary"
    Closure<String> queryPostRequestBodyClosure = { String commandResponseBookingOfferIdentifier ->
      """
      {
        "userIdentifier": "standard-customer@cargotracker.com",
        "bookingOfferIdentifier": "${ commandResponseBookingOfferIdentifier }"
      }
      """
    }

    Request commandRequest = Request.Post(createBookingOfferCommandUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseBookingOfferIdentifier = commandResponseJson.payload.bookingOfferId.identifier

    then:
    commandResponseStatusCode == 200
    commandResponseBookingOfferIdentifier

    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      // given
      Request queryRequest = Request.Post(bookingOfferSummaryQueryUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(queryPostRequestBodyClosure(commandResponseBookingOfferIdentifier), ContentType.APPLICATION_JSON)

      // when
      HttpResponse queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

      // then:
      queryResponseStatusCode == 200
      verifyAll(queryResponseJson.payload as Map, {
        it.size() == 11

        bookingOfferIdentifier == commandResponseBookingOfferIdentifier

        customerType == "STANDARD"

        originLocationUnLoCode == "HRRJK"
        originLocationName == "Rijeka"
        originLocationCountryName == "Croatia"

        destinationLocationUnLoCode == "NLRTM"
        destinationLocationName == "Rotterdam"
        destinationLocationCountryName == "Netherlands"

        firstEventRecordedAt == lastEventRecordedAt
        Instant.parse(firstEventRecordedAt as String) > currentTime
        Instant.parse(lastEventRecordedAt as String) > currentTime
        lastEventSequenceNumber == 0
      })
    }

    where:
    acceptLanguageHeaderParam | _
    "hr-HR"                   | _
    "en"                      | _
  }

  void "command - should not create booking offer for invalid command: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/booking-offer/create-booking-offer"
    String commandPostRequestBody = """
        {
          "userIdentifier": "standard-customer@cargotracker.com",
          "routeSpecification": {
            "originLocation": "NLRTM",
            "destinationLocation": "HRZAG",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }",
            "arrivalLatestTime": "${ arrivalLatestTime }"
          },
          "commodityInfo": {
            "commodityType": "chilled",
            "totalWeightInKilograms": 1000,
            "requestedStorageTemperatureInCelsius": 5
          },
          "containerDimensionType": "DIMENSION_ISO_22"
        }
        """

    Request commandRequest = Request.Post(createBookingOfferCommandUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

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

  void "query - should not find non-existing booking offer: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideApp.host }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/booking-offer/booking-offer-summary"
    String queryPostRequestBody = """
      {
        "userIdentifier": "standard-customer@cargotracker.com",
        "bookingOfferIdentifier": "${ UUID.randomUUID() }"
      }
      """

    Request queryRequest = Request.Post(bookingOfferSummaryQueryUrl)
                                  .addHeader("Content-Type", "application/json")
                                  .addHeader("Accept", "application/json")
                                  .addHeader("Accept-Charset", "utf-8")
                                  .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                  .bodyString(queryPostRequestBody, ContentType.APPLICATION_JSON)

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
