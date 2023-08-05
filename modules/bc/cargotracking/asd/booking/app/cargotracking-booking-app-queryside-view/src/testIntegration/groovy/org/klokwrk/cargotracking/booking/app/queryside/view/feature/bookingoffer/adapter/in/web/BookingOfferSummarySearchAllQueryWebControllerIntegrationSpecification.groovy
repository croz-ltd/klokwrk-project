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
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared

import javax.sql.DataSource
import java.nio.charset.Charset

import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseHasPageablePayloadThat
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder.bookingOfferSummarySearchAllQueryRequest_originOfRijeka
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder.bookingOfferSummarySearchAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.data.SortRequirementJsonFixtureBuilder.sortRequirement_default
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.bookingOfferSummarySearchAll_failed
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestHelpers.bookingOfferSummarySearchAll_succeeded
import static org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SuppressWarnings("GroovyAccessibility")
@EnableSharedInjection
@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummarySearchAllQueryWebControllerIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Shared
  @Autowired
  EventBus eventBus

  @Shared
  @Autowired
  Sql groovySql

  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setupSpec() {
    makeForSearch_pastBookingOfferCreatedEvents().each {
      publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql, it)
    }
  }

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).defaultResponseCharacterEncoding(Charset.forName("UTF-8")).build()
  }

  void "should work for search request with default paging and sorting"() {
    when:
    Map responseMap = bookingOfferSummarySearchAll_succeeded(
        bookingOfferSummarySearchAllQueryRequest_originOfRijeka()
            .totalCommodityWeightFromIncluding(5_000.kg)
            .totalCommodityWeightToIncluding(50_000.kg)
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    // Here we are using closure-style top-level API.
    assertResponseHasMetaDataThat(responseMap) {
      isSuccessful()
      has_general_locale(localeStringParam)
    }

    assertResponseHasPageablePayloadThat(responseMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat {
        hasPageElementsCountGreaterThenOrEqual(5)
      }
      hasPageContentWithFirstItemThat {
        hasCustomerTypeOfStandard()
        hasOriginLocationOfRijeka()
        hasDestinationLocationName("Los Angeles")
        hasTotalCommodityWeight(15000.kg)
        hasTotalContainerTeuCount(1.00G)
        hasLastEventSequenceNumber(0)
      }
    }

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "should work for search request with default paging and sorting but with empty page content"() {
    when:
    Map responseMap = bookingOfferSummarySearchAll_succeeded(
        bookingOfferSummarySearchAllQueryRequest_standardCustomer()
            .userId("platinum-customer@cargotracker.com")
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    // Here we are using fluent-style top-level API.
    assertResponseHasMetaDataThat(responseMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    assertResponseHasPageablePayloadThat(responseMap)
        .isSuccessfulAndEmpty()

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "should fail for invalid property name in sort requirements"() {
    when:
    Map responseMap = bookingOfferSummarySearchAll_failed(
        bookingOfferSummarySearchAllQueryRequest_standardCustomer()
            .sortRequirementList([sortRequirement_default().propertyName("nonExistingProperty")])
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap) {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(messageParam)
    }

    assertResponseHasPageablePayloadThat(responseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | messageParam
    "hr-HR"             | "hr_HR"           | "Nije moguće sortirati po podatku s nazivom 'nonExistingProperty'. Naziv ne postoji."
    "en"                | "en"              | "Can't sort by property with name 'nonExistingProperty'. Property name does not exist."
  }
}
