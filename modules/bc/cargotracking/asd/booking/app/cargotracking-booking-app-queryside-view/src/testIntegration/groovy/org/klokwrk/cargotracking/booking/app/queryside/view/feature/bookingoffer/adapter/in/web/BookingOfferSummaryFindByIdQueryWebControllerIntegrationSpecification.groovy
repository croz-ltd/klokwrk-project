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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.app.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import javax.sql.DataSource
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindByIdQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.BOOKING_OFFER_SUMMARY_FIND_BY_ID_URL_PATH
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.bookingOfferSummaryFindById_failed
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.bookingOfferSummaryFindById_failedNotFound
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.bookingOfferSummaryFindById_succeeded
import static org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummaryFindByIdQueryWebControllerIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).defaultResponseCharacterEncoding(Charset.forName("UTF-8")).build()
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should work for correct request"() {
    given:
    Instant startedAt = Instant.now()
    String myBookingOfferId = publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql)

    when:
    Map responseMap = bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(myBookingOfferId)
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap) {
      isSuccessful()
      has_general_locale(localeStringParam)
    }

    assertResponseHasPayloadThat(responseMap) {
      isSuccessful()
      hasBookingOfferId(myBookingOfferId)
      hasCustomerTypeOfStandard()
      hasCommodityOfDryTypeWithDefaultWeight()
      hasOriginLocationOfRijeka()
      hasDestinationLocationOfRotterdam()
      hasDepartureEarliestTimeGreaterThan(startedAt + Duration.ofHours(1))
      hasDepartureLatestTimeGreaterThan(startedAt + Duration.ofHours(2))
      hasArrivalLatestTimeGreaterThan(startedAt + Duration.ofHours(3))
      hasEventMetadataOfTheFirstEventWithCorrectTiming(startedAt)
    }

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should return expected response when request is not valid - validation failure"() {
    when:
    Map responseMap = bookingOfferSummaryFindById_failed(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(null)
            .userId(null)
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap) {
      isViolationOfValidation()
      has_general_locale(localeStringParam)
      has_violation_message(myViolationMessageParam)
    }

    // NOTE: In *Assertion classes, we don't need to have a method for validating every single part of the response. We can always turn to native Spock means of verification.
    //       However, if that specific assertion often repeats, this can be a reason for introducing a specialized method in the *Assertion class.
    verifyAll(responseMap.metaData.violation.validationReport as Map) {
      root.type == "bookingOfferSummaryFindByIdQueryRequest"

      verifyAll(constraintViolations as List<Map>) {
        size() == 2
        it.find({ it.path == "bookingOfferId" }).type == "notBlank"
        it.find({ it.path == "userId" }).type == "notBlank"
      }
    }

    assertResponseHasPayloadThat(responseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | myViolationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }

  void "should return expected response when specified user can not be found - stateful validation failure"() {
    given:
    String myBookingOfferId = publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql)

    when:
    Map responseMap = bookingOfferSummaryFindById_failed(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(myBookingOfferId)
            .userId("someUserId")
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap) {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(myViolationMessageParam)
    }

    assertResponseHasPayloadThat(responseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | myViolationMessageParam
    "hr-HR"             | "hr_HR"           | "Nije pronađen potrošač s korisničkim imenom 'someUserId'."
    "en"                | "en"              | "Can't find the customer with user id 'someUserId'."
  }

  void "should return expected response when BookingOfferSummary cannot be found - domain failure"() {
    when:
    Map responseMap = bookingOfferSummaryFindById_failedNotFound(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(myViolationMessageParam)
    }

    assertResponseHasPayloadThat(responseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | myViolationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  void "should return expected response for a request with invalid HTTP method"() {
    when:
    MvcResult mvcResult = mockMvc.perform(put(BOOKING_OFFER_SUMMARY_FIND_BY_ID_URL_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageParam)).andReturn()
    Map responseMap = objectMapper.readValue(mvcResult.response.contentAsString, Map)

    then:
    mvcResult.response.status == HttpStatus.METHOD_NOT_ALLOWED.value()
    mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    assertResponseHasMetaDataThat(responseMap) {
      isViolationOfInfrastructureWeb_methodNotAllowed()
      has_general_locale(localeStringParam)
      has_violation_message(myViolationMessageParam)
    }

    assertResponseHasPayloadThat(responseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | myViolationMessageParam
    "hr-HR"             | "hr_HR"           | "Zahtjev nije ispravan."
    "en"                | "en"              | "Request is not valid."
  }
}
