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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lang.groovy.misc.InstantUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoBookingWebControllerIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  @SuppressWarnings(["CodeNarc.AbcMetric", "CodeNarc.MethodSize"])
  void "should work for correct request - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String myBookingOfferIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            bookingOfferIdentifier: myBookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK",
                departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
                arrivalLatestTime: arrivalLatestTime
            ],
            commodityInfo: [
                commodityType: "dry",
                totalWeightInKilograms: 1000
            ],
            containerDimensionType: "DIMENSION_ISO_22"
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      size() == 3
      locale == localeStringParam
      severity == Severity.INFO.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      size() == 2
      message == HttpStatus.OK.reasonPhrase
      status == HttpStatus.OK.value().toString()
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 3
      bookingOfferId.identifier == myBookingOfferIdentifier
      routeSpecification
      bookingOfferCommodities
    }

    verifyAll(responseContentMap.payload.routeSpecification as Map) {
      size() == 5
      originLocation
      destinationLocation
      it.departureEarliestTime == InstantUtils.roundUpInstantToTheHour(departureEarliestTime).toString()
      it.departureLatestTime == InstantUtils.roundUpInstantToTheHour(departureLatestTime).toString()
      it.arrivalLatestTime == InstantUtils.roundUpInstantToTheHour(arrivalLatestTime).toString()
    }

    verifyAll(responseContentMap.payload.routeSpecification.originLocation as Map) {
      size() == 4
      name == "Rotterdam"
      countryName == "Netherlands"

      unLoCode

      unLoCode.code
      unLoCode.code.encoded == "NLRTM"

      unLoCode.function
      unLoCode.function.encoded == "12345---"

      unLoCode.coordinates
      unLoCode.coordinates.encoded == "5155N 00430E"

      portCapabilities
      portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
    }

    verifyAll(responseContentMap.payload.routeSpecification.destinationLocation as Map) {
      size() == 4
      name == "Rijeka"
      countryName == "Croatia"

      unLoCode

      unLoCode.code
      unLoCode.code.encoded == "HRRJK"

      unLoCode.coordinates
      unLoCode.coordinates.encoded == "4520N 01424E"

      unLoCode.function
      unLoCode.function.encoded == "1234----"

      portCapabilities
      portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
    }

    verifyAll(responseContentMap.payload.bookingOfferCommodities as Map) {
      size() == 3

      commodityTypeToCommodityMap == [
          DRY: [
              containerType: "TYPE_ISO_22G1",
              commodityInfo: [
                  commodityType: "DRY",
                  totalWeight: [
                      value: 1000,
                      unit: [
                          name: "Kilogram",
                          symbol: "kg"
                      ]
                  ]
              ],
              maxAllowedWeightPerContainer: [
                  value: 23750,
                  unit: [
                      name: "Kilogram",
                      symbol: "kg"
                  ]
              ],
              maxRecommendedWeightPerContainer: [
                  value: 1000,
                  unit: [
                      name: "Kilogram",
                      symbol: "kg"
                  ]
              ],
              containerCount: 1
          ]
      ]

      totalCommodityWeight == [
          value: 1000,
          unit: [
              name: "Kilogram",
              symbol: "kg"
          ]
      ]

      totalContainerCount == 1
    }

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should return expected response when request is not valid - validation failure - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    String bookingOfferIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [originLocation: null, destinationLocation: null, departureEarliestTime: null, departureLatestTime: null, arrivalLatestTime: null],
            commodityInfo: [commodityType: "dry", totalWeightInKilograms: 1000],
            containerDimensionType: "DIMENSION_ISO_22"
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      size() == 3
      locale == localeStringParam
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      size() == 2
      message == HttpStatus.BAD_REQUEST.reasonPhrase
      status == HttpStatus.BAD_REQUEST.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      size() == 4
      code == HttpStatus.BAD_REQUEST.value().toString()
      message == violationMessageParam
      type == ViolationType.VALIDATION.name().toLowerCase()
      validationReport != null
    }

    verifyAll(responseContentMap.metaData.violation.validationReport as Map) {
      size() == 2
      root.type == "createBookingOfferCommandRequest"
      constraintViolations.size() == 5
      constraintViolations.find({ it.path == "routeSpecification.originLocation" }).type == "notBlank"
      constraintViolations.find({ it.path == "routeSpecification.destinationLocation" }).type == "notBlank"
      constraintViolations.find({ it.path == "routeSpecification.departureEarliestTime" }).type == "notNull"
      constraintViolations.find({ it.path == "routeSpecification.departureLatestTime" }).type == "notNull"
      constraintViolations.find({ it.path == "routeSpecification.arrivalLatestTime" }).type == "notNull"
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }

  void "should fail when origin and destination locations are equal - domain failure - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "HRRJK", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            commodityInfo: [commodityType: "dry", totalWeightInKilograms: 1000],
            containerDimensionType: "DIMENSION_ISO_22"
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      size() == 3
      locale == localeStringParam
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      size() == 2
      message == HttpStatus.BAD_REQUEST.reasonPhrase
      status == HttpStatus.BAD_REQUEST.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      size() == 3
      code == HttpStatus.BAD_REQUEST.value().toString()
      message == violationMessageParam
      type == ViolationType.DOMAIN.name().toLowerCase()
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Navedena polazna lokacija jednaka je navedenoj ciljnoj lokaciji."
    "en"                | "en"              | "Specified origin location is equal to specified destination location."
  }

  void "should fail when cargo can not be sent to destination location - domain failure - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRZAG", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            commodityInfo: [commodityType: "dry", totalWeightInKilograms: 1000],
            containerDimensionType: "DIMENSION_ISO_22"
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      size() == 3
      locale == localeStringParam
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      size() == 2
      message == HttpStatus.BAD_REQUEST.reasonPhrase
      status == HttpStatus.BAD_REQUEST.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      size() == 3
      code == HttpStatus.BAD_REQUEST.value().toString()
      message == violationMessageParam
      type == ViolationType.DOMAIN.name().toLowerCase()
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "should fail when commodity weight is too high - domain failure - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            commodityInfo: [commodityType: "dry", totalWeightInKilograms: 125_000_000],
            containerDimensionType: "dimension_ISO_22"
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    responseContentMap.payload.size() == 0

    verifyAll(responseContentMap.metaData as Map) {
      general.severity == Severity.WARNING.name().toLowerCase()
      general.locale == localeStringParam

      http.message == HttpStatus.BAD_REQUEST.reasonPhrase
      http.status == HttpStatus.BAD_REQUEST.value().toString()

      violation.code == HttpStatus.BAD_REQUEST.value().toString()
      violation.message == violationMessageParam
      violation.type == ViolationType.DOMAIN.name().toLowerCase()
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Nije moguće prihvatiti robu jer bi premašili najveći dozvoljeni broj kontejera po ponudi za rezervaciju."
    "en"                | "en"              | "Cannot accept commodity because it would exceed the maximum allowed count of containers per a booking offer."
  }

  void "should return expected response for a request with invalid HTTP method - [acceptLanguage: #acceptLanguageParam]"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString([bookingOfferIdentifier: null, routeSpecification: null])

    when:
    MvcResult mvcResult = mockMvc.perform(
        put("/cargo-booking/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.METHOD_NOT_ALLOWED.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      size() == 3
      locale == localeStringParam
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      size() == 2
      message == HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase
      status == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      size() == 4
      code == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
      message == violationMessageParam
      type == ViolationType.INFRASTRUCTURE_WEB.name().toLowerCase()
      logUuid
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }
}
