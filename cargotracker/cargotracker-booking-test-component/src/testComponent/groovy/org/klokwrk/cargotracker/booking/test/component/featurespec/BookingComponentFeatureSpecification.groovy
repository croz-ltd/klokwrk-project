/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
  void "command - should book cargo: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)

    //noinspection HttpUrlsUsage
    String commandCargoBookUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "routeSpecification": {
            "originLocation": "HRRJK",
            "destinationLocation": "NLRTM",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }"
          }
        }
        """

    Request commandRequest = Request.Post(commandCargoBookUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseCargoIdentifier = commandResponseJson.payload.cargoId.identifier

    then:
    commandResponseStatusCode == 200
    commandResponseCargoIdentifier

    where:
    acceptLanguageHeaderParam | _
    "hr-HR"                   | _
    "en"                      | _
  }

  void "query - should find booked cargo: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)

    //noinspection HttpUrlsUsage
    String commandBookCargoUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "routeSpecification": {
            "originLocation": "HRRJK",
            "destinationLocation": "NLRTM",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }"
          }
        }
        """

    //noinspection HttpUrlsUsage
    String cargoSummaryQueryUrl = "http://${ querySideApp.containerIpAddress }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/cargo-info/cargo-summary"
    Closure<String> queryPostRequestBodyClosure = { String commandResponseCargoIdentifier ->
      """
      {
        "cargoIdentifier": "${ commandResponseCargoIdentifier }"
      }
      """
    }

    Request commandRequest = Request.Post(commandBookCargoUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseCargoIdentifier = commandResponseJson.payload.cargoId.identifier

    then:
    commandResponseStatusCode == 200
    commandResponseCargoIdentifier

    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      // given
      Request queryRequest = Request.Post(cargoSummaryQueryUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeaderParam)
                                    .bodyString(queryPostRequestBodyClosure(commandResponseCargoIdentifier), ContentType.APPLICATION_JSON)

      // when
      HttpResponse queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

      // then:
      queryResponseStatusCode == 200
      queryResponseJson.payload.cargoIdentifier == commandResponseCargoIdentifier
    }

    where:
    acceptLanguageHeaderParam | _
    "hr-HR"                   | _
    "en"                      | _
  }

  void "command - should not book cargo for invalid command: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)

    //noinspection HttpUrlsUsage
    String commandBookCargoUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "routeSpecification": {
            "originLocation": "NLRTM",
            "destinationLocation": "HRZAG",
            "departureEarliestTime": "${ departureEarliestTime }",
            "departureLatestTime": "${ departureLatestTime }"
          }
        }
        """

    Request commandRequest = Request.Post(commandBookCargoUrl)
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

  void "query - should not find non-existing cargo: [acceptLanguageHeader: #acceptLanguageHeaderParam]"() {
    given:
    //noinspection HttpUrlsUsage
    String cargoSummaryQueryUrl = "http://${ querySideApp.containerIpAddress }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/cargo-info/cargo-summary"
    String queryPostRequestBody = """
      {
        "cargoIdentifier": "${ UUID.randomUUID() }"
      }
      """

    Request queryRequest = Request.Post(cargoSummaryQueryUrl)
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
    "hr-HR"                   | "Sumarni izvještaj za željeni teret nije pronađen."
    "en"                      | "Summary report for specified cargo is not found."
  }
}
