package org.klokwrk.cargotracker.booking.test.component.test.util

import groovy.transform.CompileStatic
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.testcontainers.containers.GenericContainer

import java.time.Duration
import java.time.Instant

@CompileStatic
class FeatureTestHelpers {
  static String makeCommandRequestUrl_createBookingOffer(GenericContainer commandSideApp) {
    //noinspection HttpUrlsUsage
    String createBookingOfferCommandUrl = "http://${ commandSideApp.host }:${ commandSideApp.firstMappedPort }/cargotracker-booking-commandside/booking-offer/create-booking-offer"
    return createBookingOfferCommandUrl
  }

  static String makeQueryRequestUrl_bookingOfferSummary_findById(GenericContainer querySideViewApp) {
    //noinspection HttpUrlsUsage
    String bookingOfferSummaryQueryUrl = "http://${ querySideViewApp.host }:${ querySideViewApp.firstMappedPort }/cargotracker-booking-queryside-view/booking-offer/booking-offer-summary-find-by-id"
    return bookingOfferSummaryQueryUrl
  }

  static String makeCommandRequestBody_createBookingOffer_dryCommodity(Instant baseTime = Instant.now()) {
    Instant departureEarliestTime = baseTime + Duration.ofHours(1)
    Instant departureLatestTime = baseTime + Duration.ofHours(2)
    Instant arrivalLatestTime = baseTime + Duration.ofHours(3)

    String commandRequestBody = """
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

    return commandRequestBody
  }

  static String makeCommandRequestBody_createBookingOffer_chilledCommodity(Instant baseTime = Instant.now()) {
    Instant departureEarliestTime = baseTime + Duration.ofHours(1)
    Instant departureLatestTime = baseTime + Duration.ofHours(2)
    Instant arrivalLatestTime = baseTime + Duration.ofHours(3)

    String commandRequestBody = """
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

    return commandRequestBody
  }

  static String makeCommandRequestBody_createBookingOffer_invalid() {
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    //noinspection HttpUrlsUsage
    String commandRequestBody = """
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

    return commandRequestBody
  }

  static String makeQueryRequestBody_bookingOfferSummary_findById(String bookingOfferIdentifier) {
    String queryRequestBody = """
      {
        "userIdentifier": "standard-customer@cargotracker.com",
        "bookingOfferIdentifier": "${ bookingOfferIdentifier }"
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
