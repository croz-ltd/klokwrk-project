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
    commandResponseJson.metaData.titleDetailedText == titleDetailedTextContent

    where:
    acceptLanguageHeader | titleDetailedTextContent
    "hr-HR"              | "Vaš je zahtjev uspješno izvršen."
    "en"                 | "Your request is successfully executed."
    "hr-HR"              | "Vaš je zahtjev uspješno izvršen."
    "en"                 | "Your request is successfully executed."
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
      queryResponseJson.metaData.titleDetailedText == titleDetailedTextContent
    }

    where:
    acceptLanguageHeader | titleDetailedTextContent
    "hr-HR"              | "Vaš je zahtjev uspješno izvršen."
    "en"                 | "Your request is successfully executed."
    "hr-HR"              | "Vaš je zahtjev uspješno izvršen."
    "en"                 | "Your request is successfully executed."
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
    commandResponseJson.metaData.titleDetailedText == titleDetailedTextContent
    commandResponseJson.payload.isEmpty()

    where:
    acceptLanguageHeader | titleDetailedTextContent
    "hr-HR"              | "Teret nije prihvaćen jer ga nije moguće poslati na ciljnu lokaciju iz navedene početne lokacije."
    "en"                 | "Cargo is not booked since destination location cannot accept cargo from specified origin location."
    "hr-HR"              | "Teret nije prihvaćen jer ga nije moguće poslati na ciljnu lokaciju iz navedene početne lokacije."
    "en"                 | "Cargo is not booked since destination location cannot accept cargo from specified origin location."
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
    queryResponseJson.metaData.titleDetailedText == titleDetailedTextContent

    where:
    acceptLanguageHeader | titleDetailedTextContent
    "hr-HR"              | "Sumarni izvještaj za željeni teret nije pronađen."
    "en"                 | "Summary report for specified cargo is not found."
    "hr-HR"              | "Sumarni izvještaj za željeni teret nije pronađen."
    "en"                 | "Summary report for specified cargo is not found."
  }
}