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
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
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

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rotterdamToRijeka
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_base
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.CargoRequestDataJsonFixtureBuilder.cargoRequestData_dry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rijekaToRotterdam
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka
import static org.klokwrk.cargotracker.lib.test.support.web.WebResponseContentMetaDataAssertion.assertWebResponseContentHasMetaDataThat
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
    mockMvc ?= webAppContextSetup(webApplicationContext).defaultResponseCharacterEncoding(Charset.forName("UTF-8")).build()
  }

  void "should work for correct request"() {
    given:
    Instant currentTime = Instant.now()
    Instant departureEarliestTime = currentTime + Duration.ofHours(1)
    Instant departureLatestTime = currentTime + Duration.ofHours(2)
    Instant arrivalLatestTime = currentTime + Duration.ofHours(3)

    String myBookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_rijekaToRotterdam(currentTime)
            .bookingOfferIdentifier(myBookingOfferIdentifier)
            .cargos_add(cargoRequestData_dry().commodityWeight(commodityWeightParam))
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    verifyAll(responseContentMap) {
      size() == 2

      verifyAll(it.payload as Map) {
        size() == 4
        customer
        bookingOfferId
        routeSpecification
        bookingOfferCargos

        verifyAll(it.bookingOfferId as Map) {
          size() == 1
          identifier == myBookingOfferIdentifier
        }

        verifyAll(it.customer as Map) {
          size() == 2
          customerId == "26d5f7d8-9ded-4ce3-b320-03a75f674f4e"
          customerType == "STANDARD"
        }

        verifyAll(it.routeSpecification as Map) {
          size() == 5
          originLocation
          destinationLocation
          it.departureEarliestTime == InstantUtils.roundUpInstantToTheHour(departureEarliestTime).toString()
          it.departureLatestTime == InstantUtils.roundUpInstantToTheHour(departureLatestTime).toString()
          it.arrivalLatestTime == InstantUtils.roundUpInstantToTheHour(arrivalLatestTime).toString()

          verifyAll(it.originLocation as Map) {
            size() == 4
            name == "Rijeka"
            countryName == "Croatia"
            portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
            unLoCode

            verifyAll(it.unLoCode as Map) {
              size() == 3
              code
              coordinates
              function

              verifyAll(it.code as Map) {
                size() == 3
                encoded == "HRRJK"
                countryCode == "HR"
                locationCode == "RJK"
              }

              verifyAll(it.coordinates as Map) {
                size() == 3
                encoded == "4520N 01424E"
                latitudeInDegrees == 45.33
                longitudeInDegrees == 14.4
              }

              verifyAll(it.function as Map) {
                size() == 7
                encoded == "1234----"
                isPort == true
                isRailTerminal == true
                isRoadTerminal == true
                isAirport == true
                isPostalExchangeOffice == false
                isBorderCrossing == false
              }
            }
          }

          verifyAll(it.destinationLocation as Map) {
            size() == 4
            name == "Rotterdam"
            countryName == "Netherlands"
            portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
            unLoCode

            verifyAll(it.unLoCode as Map) {
              size() == 3
              code
              coordinates
              function

              verifyAll(it.code as Map) {
                size() == 3
                encoded == "NLRTM"
                countryCode == "NL"
                locationCode == "RTM"
              }

              verifyAll(it.coordinates as Map) {
                size() == 3
                encoded == "5155N 00430E"
                latitudeInDegrees == 51.92
                longitudeInDegrees == 4.5
              }

              verifyAll(it.function as Map) {
                size() == 7
                encoded == "12345---"
                isPort == true
                isRailTerminal == true
                isRoadTerminal == true
                isAirport == true
                isPostalExchangeOffice == true
                isBorderCrossing == false
              }
            }
          }
        }

        verifyAll(it.bookingOfferCargos as Map) {
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
      }
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
        createBookingOfferCommandRequest_rotterdamToRijeka(currentTime)
            .bookingOfferIdentifier(myBookingOfferIdentifier)
            .cargos_add(
                cargoRequestData_base()
                    .commodityType("${ commodityTypeParam.name().toLowerCase() }")
                    .commodityRequestedStorageTemperature(commodityRequestedStorageTemperatureParam)
            )
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    verifyAll(responseContentMap) {
      size() == 2

      verifyAll(it.payload as Map) {
        size() == 4
        bookingOfferId
        customer
        routeSpecification
        bookingOfferCargos

        verifyAll(it.bookingOfferId as Map) {
          size() == 1
          identifier == myBookingOfferIdentifier
        }

        verifyAll(it.customer as Map) {
          size() == 2
          customerId == "26d5f7d8-9ded-4ce3-b320-03a75f674f4e"
          customerType == "STANDARD"
        }

        verifyAll(it.routeSpecification as Map) {
          size() == 5
          originLocation
          destinationLocation
          it.departureEarliestTime == InstantUtils.roundUpInstantToTheHour(departureEarliestTime).toString()
          it.departureLatestTime == InstantUtils.roundUpInstantToTheHour(departureLatestTime).toString()
          it.arrivalLatestTime == InstantUtils.roundUpInstantToTheHour(arrivalLatestTime).toString()

          verifyAll(it.originLocation as Map) {
            size() == 4
            name == "Rotterdam"
            countryName == "Netherlands"
            portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
            unLoCode

            verifyAll(it.unLoCode as Map) {
              code.encoded == "NLRTM"
              function.encoded == "12345---"
              coordinates.encoded == "5155N 00430E"
            }
          }

          verifyAll(it.destinationLocation as Map) {
            size() == 4
            name == "Rijeka"
            countryName == "Croatia"
            portCapabilities == ["CONTAINER_PORT", "SEA_PORT"]
            unLoCode

            verifyAll(it.unLoCode as Map) {
              code.encoded == "HRRJK"
              coordinates.encoded == "4520N 01424E"
              function.encoded == "1234----"
            }
          }
        }

        verifyAll(it.bookingOfferCargos as Map) {
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
      }
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
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_cargoDry()
            .routeSpecification(
                new RouteSpecificationRequestDataJsonFixtureBuilder()
                    .originLocation(null)
                    .destinationLocation(null)
                    .departureLatestTime(null)
                    .departureLatestTime(null)
                    .arrivalLatestTime(null)
            )
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfValidation()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap.metaData.violation.validationReport as Map) {
      root.type == "createBookingOfferCommandRequest"

      verifyAll(constraintViolations as List<Map>) {
        size() == 5
        it.find({ it.path == "routeSpecification.originLocation" }).type == "notBlank"
        it.find({ it.path == "routeSpecification.destinationLocation" }).type == "notBlank"
        it.find({ it.path == "routeSpecification.departureEarliestTime" }).type == "notNull"
        it.find({ it.path == "routeSpecification.departureLatestTime" }).type == "notNull"
        it.find({ it.path == "routeSpecification.arrivalLatestTime" }).type == "notNull"
      }
    }

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }

  void "should fail when customer cannot be found - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry()
            .userIdentifier("unknownUserIdentifier")
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Nije pronađen potrošač s korisničkim imenom 'unknownUserIdentifier'."
    "en"                | "en"              | "Can't find the customer with user id 'unknownUserIdentifier'."
  }

  void "should fail when origin and destination locations are equal - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_cargoDry()
            .routeSpecification(routeSpecificationRequestData_rijekaToRotterdam().destinationLocation("HRRJK"))
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Navedena polazna lokacija jednaka je navedenoj ciljnoj lokaciji."
    "en"                | "en"              | "Specified origin location is equal to specified destination location."
  }

  void "should fail when cargo can not be sent to destination location - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_cargoDry()
            .routeSpecification(routeSpecificationRequestData_rotterdamToRijeka().destinationLocation("HRZAG"))
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "should fail when commodity weight is too high - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_rijekaToRotterdam()
            .cargos([cargoRequestData_dry().commodityWeight(125_000_000.kg)])
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Nije moguće prihvatiti teret jer bi premašili najveći dozvoljeni broj TEU jedinica (5000 TEU) po ponudi za rezervaciju."
    "en"                | "en"              | "Cannot accept cargo because it would exceed the maximum allowed count of TEU units (5000 TEU) per a booking offer."
  }

  void "should fail when commodity requested storage temperature is supplied but not supported for commodity type - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_rotterdamToRijeka()
            .cargos([cargoRequestData_dry().commodityRequestedStorageTemperature(1.degC)])
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtijevana temperatura skladištenja nije dozvoljena za DRY tip robe."
    "en"                | "en"              | "Requested storage temperature is not supported for DRY commodity type."
  }

  void "should fail when requested storage temperature is out of range - domain failure"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(
        createBookingOfferCommandRequest_rotterdamToRijeka()
            .cargos_add(
                cargoRequestData_base()
                    .commodityType(commodityTypeParam)
                    .commodityRequestedStorageTemperature(commodityRequestedStorageTemperatureParam)
            )
            .buildAsMap()
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

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfDomain_badRequest()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
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
    commodityTypeParam | commodityRequestedStorageTemperatureParam | violationMessageParam
    "air_cooled"       | 13.degC                                   | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za zrakom hlađenu robu: [2, 12] Celzija."
    "air_cooled"       | 1.degC                                    | "Requested storage temperature is not in supported range for air cooled commodities: [2, 12] Celsius."

    "chilled"          | 7.degC                                    | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za rashlađenu robu: [-2, 6] Celzija."
    "chilled"          | -3.degC                                   | "Requested storage temperature is not in supported range for chilled commodities: [-2, 6] Celsius."

    "frozen"           | -7.degC                                   | "Zahtijevana temperatura skladištenja nije u dozvoljenom intervalu za smrznutu robu: [-20, -8] Celzija."
    "frozen"           | -21.degC                                  | "Requested storage temperature is not in supported range for frozen commodities: [-20, -8] Celsius."
  }

  void "should return expected response for a request with invalid HTTP method"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString(new CreateBookingOfferCommandRequestJsonFixtureBuilder().buildAsMap())

    when:
    MvcResult mvcResult = mockMvc.perform(
        put("/booking-offer/create-booking-offer")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.METHOD_NOT_ALLOWED.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertWebResponseContentHasMetaDataThat(responseContentMap)
        .isViolationOfInfrastructureWeb_methodNotAllowed()
        .has_general_locale(localeStringParam)
        .has_violation_message(violationMessageParam)

    verifyAll(responseContentMap) {
      size() == 2
      metaData

      verifyAll(responseContentMap.payload as Map) {
        size() == 0
      }
    }

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }
}
