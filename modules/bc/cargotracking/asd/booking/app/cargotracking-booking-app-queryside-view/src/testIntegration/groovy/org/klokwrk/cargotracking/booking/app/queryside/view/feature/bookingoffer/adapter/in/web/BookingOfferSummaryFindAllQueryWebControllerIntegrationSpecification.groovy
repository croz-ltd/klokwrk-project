/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.cargotracking.booking.app.queryside.view.test.base.AbstractQuerySide_forFindAllAndSearchAllTests_IntegrationSpecification
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
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.data.PageRequirementJsonFixtureBuilder.pageRequirement_default
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.data.SortRequirementJsonFixtureBuilder.sortRequirement_default
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestRequestHelpers.bookingOfferSummaryFindAll_failed
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestRequestHelpers.bookingOfferSummaryFindAll_succeeded
import static org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@EnableSharedInjection
@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummaryFindAllQueryWebControllerIntegrationSpecification extends AbstractQuerySide_forFindAllAndSearchAllTests_IntegrationSpecification {
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
    setupProjection_forFindAllAndSearchAllTests(eventBus, groovySql)
  }

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).defaultResponseCharacterEncoding(Charset.forName("UTF-8")).build()
  }

  void "should work for correct request with default paging and sorting"() {
    when:
    Map responseMap = bookingOfferSummaryFindAll_succeeded(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer().buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
    assertResponseHasMetaDataThat(responseMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    assertResponseHasPageablePayloadThat(responseMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat({
        hasPageElementsCount(25)
        hasTotalElementsCount(25)
      })
      hasPageContentWithAllItemsThat({
        hasCustomerTypeOfStandard()
      })
    }

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "should work for correct request with default paging and sorting but with empty page content"() {
    when:
    Map responseMap = bookingOfferSummaryFindAll_succeeded(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer()
            .userId("platinum-customer@cargotracking.com")
            .buildAsJsonString(),
        acceptLanguageParam,
        mockMvc
    )

    then:
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
    Map responseMap = bookingOfferSummaryFindAll_failed(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer()
            .pageRequirement(pageRequirement_default().size(3)) // Not needed, but added for demonstration purposes
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
