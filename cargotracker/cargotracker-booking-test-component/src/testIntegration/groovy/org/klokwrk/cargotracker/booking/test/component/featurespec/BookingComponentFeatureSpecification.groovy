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

class BookingComponentFeatureSpecification extends AbstractComponentIntegrationSpecification {
  void "should book cargo for correct command: [acceptLanguageHeader: #acceptLanguageHeader]"() {
    given:
    String commandCargoBookUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "originLocation": "HRRJK",
          "destinationLocation": "HRZAG"
        }
        """

    Request commandRequest = Request.Post(commandCargoBookUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeader)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseAggregateIdentifier = commandResponseJson.payload.aggregateIdentifier

    then:
    commandResponseStatusCode == 200
    commandResponseAggregateIdentifier

    where:
    acceptLanguageHeader | _
    "hr-HR"              | _
    "en"                 | _
  }

  void "should query successfully for booked cargo: [acceptLanguageHeader: #acceptLanguageHeader]"() {
    given:
    String commandBookCargoUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "originLocation": "HRRJK",
          "destinationLocation": "HRZAG"
        }
        """

    String fetchCargoSummaryQueryUrl = "http://${ querySideApp.containerIpAddress }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/cargo-summary/fetch-cargo-summary"
    Closure<String> queryPostRequestBodyClosure = { String commandResponseAggregateIdentifier ->
      """
      {
        "aggregateIdentifier": "${ commandResponseAggregateIdentifier }"
      }
      """
    }

    Request commandRequest = Request.Post(commandBookCargoUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeader)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)
    String commandResponseAggregateIdentifier = commandResponseJson.payload.aggregateIdentifier

    then:
    commandResponseStatusCode == 200
    commandResponseAggregateIdentifier

    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      // given
      Request queryRequest = Request.Post(fetchCargoSummaryQueryUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeader)
                                    .bodyString(queryPostRequestBodyClosure(commandResponseAggregateIdentifier), ContentType.APPLICATION_JSON)

      // when
      HttpResponse queryResponse = queryRequest.execute().returnResponse()
      Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
      Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

      // then:
      queryResponseStatusCode == 200
      queryResponseJson.payload.aggregateIdentifier == commandResponseAggregateIdentifier
    }

    where:
    acceptLanguageHeader | _
    "hr-HR"              | _
    "en"                 | _
  }

  void "should not book cargo for invalid command: [acceptLanguageHeader: #acceptLanguageHeader]"() {
    given:
    String commandBookCargoUrl = "http://${ commandSideApp.containerIpAddress }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/cargo-booking/book-cargo"
    String commandPostRequestBody = """
        {
          "originLocation": "HRKRK",
          "destinationLocation": "HRZAG"
        }
        """

    Request commandRequest = Request.Post(commandBookCargoUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("Accept-Language", acceptLanguageHeader)
                                    .bodyString(commandPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode

    Object commandResponseJson = new JsonSlurper().parseText(commandResponse.entity.content.text)

    then:
    commandResponseStatusCode == 400
    commandResponseJson.metaData.violation.codeMessage == violationMessageParam
    commandResponseJson.payload.isEmpty()

    where:
    acceptLanguageHeader | violationMessageParam
    "hr-HR"              | "Teret nije moguće poslati na ciljnu lokaciju iz navedene početne lokacije."
    "en"                 | "Destination location cannot accept cargo from specified origin location."
  }

  void "should not found non-existing cargo: [acceptLanguageHeader: #acceptLanguageHeader]"() {
    given:
    String fetchCargoSummaryQueryUrl = "http://${ querySideApp.containerIpAddress }:${ querySideApp.firstMappedPort }/cargotracker-booking-queryside/cargo-summary/fetch-cargo-summary"
    String queryPostRequestBody = """
      {
        "aggregateIdentifier": "${ UUID.randomUUID() }"
      }
      """

    Request queryRequest = Request.Post(fetchCargoSummaryQueryUrl)
                                  .addHeader("Content-Type", "application/json")
                                  .addHeader("Accept", "application/json")
                                  .addHeader("Accept-Charset", "utf-8")
                                  .addHeader("Accept-Language", acceptLanguageHeader)
                                  .bodyString(queryPostRequestBody, ContentType.APPLICATION_JSON)

    when:
    HttpResponse queryResponse = queryRequest.execute().returnResponse()
    Integer queryResponseStatusCode = queryResponse.statusLine.statusCode
    Object queryResponseJson = new JsonSlurper().parseText(queryResponse.entity.content.text)

    then:
    queryResponseStatusCode == 404
    queryResponseJson.payload.isEmpty()
    queryResponseJson.metaData.violation.codeMessage == violationMessageParam

    where:
    acceptLanguageHeader | violationMessageParam
    "hr-HR"              | "Sumarni izvještaj za željeni teret nije pronađen."
    "en"                 | "Summary report for specified cargo is not found."
  }
}
