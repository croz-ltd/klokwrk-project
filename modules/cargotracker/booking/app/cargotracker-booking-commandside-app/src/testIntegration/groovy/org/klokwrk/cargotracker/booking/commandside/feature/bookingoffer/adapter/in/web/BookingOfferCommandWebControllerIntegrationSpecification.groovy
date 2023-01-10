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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils
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

@SuppressWarnings("CodeNarc.AbcMetric")
@SpringBootTest(properties = ['axon.axonserver.servers = ${axonServerFirstInstanceUrl}'])
@ActiveProfiles("testIntegration")
class BookingOfferCommandWebControllerIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  void "should work for correct request"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String myBookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: myBookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK",
                departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
                arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: commodityWeightParam.value,
                        unitSymbol: "${ commodityWeightParam.unit }"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
      size() == 4
      customer
      bookingOfferId.identifier == myBookingOfferIdentifier
      routeSpecification
      bookingOfferCargos
    }

    verifyAll(responseContentMap.payload.customer as Map) {
      size() == 2
      customerId == "26d5f7d8-9ded-4ce3-b320-03a75f674f4e"
      customerType == "STANDARD"
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

    verifyAll(responseContentMap.payload.bookingOfferCargos as Map) {
      size() == 3

      bookingOfferCargoCollection == [
          [
              containerType: "TYPE_ISO_22G1",
              commodity: [
                  commodityType: "DRY",
                  weight: [
                      value: 1000,
                      unitSymbol: "kg"
                  ]
              ],
              maxAllowedWeightPerContainer: [
                  value: 20615,
                  unitSymbol: "kg"
              ],
              maxRecommendedWeightPerContainer: [
                  value: 1000,
                  unitSymbol: "kg"
              ],
              containerCount: 1,
              containerTeuCount: 1
          ]
      ]

      totalCommodityWeight == [
          value: 1000,
          unitSymbol: "kg"
      ]

      totalContainerTeuCount == 1
    }

    where:
    acceptLanguageParam | localeStringParam | commodityWeightParam
    "hr-HR"             | "hr_HR"           | 1000.kg
    "hr-HR"             | "hr_HR"           | 999.1.kg
    "hr-HR"             | "hr_HR"           | 1.t
    "hr-HR"             | "hr_HR"           | 2204.lb
    "en"                | "en"              | 1000.kg
    "en"                | "en"              | 999.1.kg
    "en"                | "en"              | 1.t
    "en"                | "en"              | 2204.lb
  }

  void "should work for correct request with commodity requested storage temperature"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String myBookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: myBookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK",
                departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
                arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "${ commodityTypeParam.name().toLowerCase() }",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    commodityRequestedStorageTemperature: [
                        value: commodityRequestedStorageTemperatureParam.value,
                        unitSymbol: "${ commodityRequestedStorageTemperatureParam.unit }"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
      size() == 4
      customer
      bookingOfferId.identifier == myBookingOfferIdentifier
      routeSpecification
      bookingOfferCargos
    }

    verifyAll(responseContentMap.payload.customer as Map) {
      size() == 2
      customerId == "26d5f7d8-9ded-4ce3-b320-03a75f674f4e"
      customerType == "STANDARD"
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

    verifyAll(responseContentMap.payload.bookingOfferCargos as Map) {
      size() == 3

      bookingOfferCargoCollection == [
          [
              containerType: "TYPE_ISO_22R1_STANDARD_REEFER",
              commodity: [
                  commodityType: "${ commodityTypeParam.name() }",
                  weight: [
                      value: 1000,
                      unitSymbol: "kg"
                  ],
                  requestedStorageTemperature: [
                      value: expectedCommodityRequestedStorageTemperatureParam.value,
                      unitSymbol: "${ expectedCommodityRequestedStorageTemperatureParam.unit }"
                  ]
              ],
              maxAllowedWeightPerContainer: [
                  value: 20520,
                  unitSymbol: "kg"
              ],
              maxRecommendedWeightPerContainer: [
                  value: 1000,
                  unitSymbol: "kg"
              ],
              containerCount: 1,
              containerTeuCount: 1
          ]
      ]

      totalCommodityWeight == [
          value: 1000,
          unitSymbol: "kg"
      ]

      totalContainerTeuCount == 1
    }

    where:
    acceptLanguageParam | localeStringParam | commodityTypeParam       | commodityRequestedStorageTemperatureParam | expectedCommodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.AIR_COOLED | 6.degC                                    | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.AIR_COOLED | 6.15.degC                                 | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.AIR_COOLED | 42.degF                                   | 5.56.degC
    "en"                | "en"              | CommodityType.AIR_COOLED | 6.degC                                    | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.AIR_COOLED | 6.15.degC                                 | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.AIR_COOLED | 43.degF                                   | 6.11.degC

    "hr-HR"             | "hr_HR"           | CommodityType.CHILLED    | 0.degC                                    | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.CHILLED    | 0.12.degC                                 | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.CHILLED    | 31.degF                                   | -0.56.degC
    "en"                | "en"              | CommodityType.CHILLED    | 0.degC                                    | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.CHILLED    | 0.12.degC                                 | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.CHILLED    | 33.degF                                   | 0.56.degC

    "hr-HR"             | "hr_HR"           | CommodityType.FROZEN     | -12.degC                                  | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.FROZEN     | -12.55.degC                               | commodityRequestedStorageTemperatureParam
    "hr-HR"             | "hr_HR"           | CommodityType.FROZEN     | 10.degF                                   | -12.22.degC
    "en"                | "en"              | CommodityType.FROZEN     | -12.degC                                  | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.FROZEN     | -12.55.degC                               | commodityRequestedStorageTemperatureParam
    "en"                | "en"              | CommodityType.FROZEN     | 11.degF                                   | -11.67.degC
  }

  void "should return expected response when request is not valid - validation failure"() {
    given:
    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [originLocation: null, destinationLocation: null, departureEarliestTime: null, departureLatestTime: null, arrivalLatestTime: null],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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

  void "should fail when customer cannot be found - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String myBookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "unknownUserIdentifier",
            bookingOfferIdentifier: myBookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK",
                departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
                arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
    "hr-HR"             | "hr_HR"           | "Nije pronađen potrošač s korisničkim imenom 'unknownUserIdentifier'."
    "en"                | "en"              | "Can't find the customer with user id 'unknownUserIdentifier'."
  }

  void "should fail when origin and destination locations are equal - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "HRRJK", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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

  void "should fail when cargo can not be sent to destination location - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRZAG", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    containerDimensionType: "DIMENSION_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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

  void "should fail when commodity weight is too high - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 125_000_000,
                        unitSymbol: "kg"
                    ],
                    containerDimensionType: "dimension_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
    "hr-HR"             | "hr_HR"           | "Nije moguće prihvatiti teret jer bi premašili najveći dozvoljeni broj TEU jedinica (5000 TEU) po ponudi za rezervaciju."
    "en"                | "en"              | "Cannot accept cargo because it would exceed the maximum allowed count of TEU units (5000 TEU) per a booking offer."
  }

  void "should fail when commodity requested storage temperature is supplied but not supported for commodity type - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "dry",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    commodityRequestedStorageTemperature: [
                        value: commodityRequestedStorageTemperatureDegCParam,
                        unitSymbol: "°C"
                    ],
                    containerDimensionType: "dimension_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
    acceptLanguageParam | localeStringParam | commodityRequestedStorageTemperatureDegCParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | 1                                             | "Zahtijevana temperatura skladištenja nije dozvoljena za DRY tip robe."
    "en"                | "en"              | 1                                             | "Requested storage temperature is not supported for DRY commodity type."
  }

  void "should fail when requested storage temperature is out of range - domain failure"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String bookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        [
            userIdentifier: "standard-customer@cargotracker.com",
            bookingOfferIdentifier: bookingOfferIdentifier,
            routeSpecification: [
                originLocation: "NLRTM", destinationLocation: "HRRJK", departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime, arrivalLatestTime: arrivalLatestTime
            ],
            cargos: [
                [
                    commodityType: "$commodityTypeStringParam",
                    commodityWeight: [
                        value: 1000,
                        unitSymbol: "kg"
                    ],
                    commodityRequestedStorageTemperature: [
                        value: commodityRequestedStorageTemperatureDegCParam,
                        unitSymbol: "°C"
                    ],
                    containerDimensionType: "dimension_ISO_22"
                ]
            ]
        ]
    )

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/create-booking-offer")
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
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"

    "hr-HR"             | "hr_HR"
    "en"                | "en"

    "hr-HR"             | "hr_HR"
    "en"                | "en"
    ___
    commodityTypeStringParam | commodityRequestedStorageTemperatureDegCParam | violationMessageParam
    "air_cooled"             | 13                                            | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za zrakom hlađenu robu: [2, 12] Celzija."
    "air_cooled"             | 1                                             | "Requested storage temperature is not in supported range for air cooled commodities: [2, 12] Celsius."

    "chilled"                | 7                                             | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za rashlađenu robu: [-2, 6] Celzija."
    "chilled"                | -3                                            | "Requested storage temperature is not in supported range for chilled commodities: [-2, 6] Celsius."

    "frozen"                 | -7                                            | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za smrznutu robu: [-20, -8] Celzija."
    "frozen"                 | -21                                           | "Requested storage temperature is not in supported range for frozen commodities: [-20, -8] Celsius."
  }

  void "should return expected response for a request with invalid HTTP method"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString([bookingOfferIdentifier: null, routeSpecification: null])

    when:
    MvcResult mvcResult = mockMvc.perform(
        put("/booking-offer/create-booking-offer")
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
