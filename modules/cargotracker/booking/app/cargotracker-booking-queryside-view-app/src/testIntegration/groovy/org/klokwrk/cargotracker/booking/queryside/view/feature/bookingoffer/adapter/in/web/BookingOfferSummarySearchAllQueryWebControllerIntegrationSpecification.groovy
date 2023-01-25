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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.spockframework.spring.EnableSharedInjection
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
import spock.lang.Shared

import javax.sql.DataSource
import java.nio.charset.Charset

import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseContentHasPageablePayloadThat
import static org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentMetaDataAssertion.assertResponseContentHasMetaDataThat
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
    given:
    String webRequestBody = objectMapper.writeValueAsString([
        userIdentifier: "standard-customer@cargotracker.com",
        customerTypeSearchList: [CustomerType.STANDARD, CustomerType.GOLD],
        originLocationName: "Rijeka",
        totalCommodityWeightFromIncluding: [
            value: 5_000,
            unitSymbol: "kg"
        ],
        totalCommodityWeightToIncluding: [
            value: 50_000,
            unitSymbol: "kg"
        ],
    ])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/booking-offer-summary-search-all")
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

    // Here we are using closure-style top-level API.
    assertResponseContentHasMetaDataThat(responseContentMap, {
      isSuccessful()
      has_general_locale(localeStringParam)
    })

    assertResponseContentHasPageablePayloadThat(responseContentMap, {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat({
        hasPageElementsCountGreaterThenOrEqual(5)
      })
      hasPageContentWithFirstElementThat({
        hasCustomerTypeOfStandard()
        hasOriginLocationOfRijeka()
        hasDestinationLocationName("Los Angeles")
        hasTotalCommodityWeight(15000.kg)
        hasTotalContainerTeuCount(1.00G)
        hasLastEventSequenceNumber(0)
      })
    })

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "should work for search request with default paging and sorting but with empty page content"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString([userIdentifier: "platinum-customer@cargotracker.com"])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/booking-offer-summary-search-all")
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

    // Here we are using fluent-style top-level API.
    assertResponseContentHasMetaDataThat(responseContentMap)
        .isSuccessful()
        .has_general_locale(localeStringParam)

    assertResponseContentHasPageablePayloadThat(responseContentMap).isSuccessfulAndEmpty()

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "should fail for invalid property name in sort requirements"() {
    String webRequestBody = objectMapper.writeValueAsString([
        userIdentifier: "standard-customer@cargotracker.com",
        customerTypeSearchList: [CustomerType.STANDARD, CustomerType.GOLD],
        pageRequirement: [ordinal: 0, size: 25],
        sortRequirementList: [
            [propertyName: "nonExistingProperty", direction: "ASC"]
        ]
    ])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/booking-offer-summary-search-all")
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

    assertResponseContentHasMetaDataThat(responseContentMap, {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(messageParam)
    })

    assertResponseContentHasPageablePayloadThat(responseContentMap).isEmpty()

    where:
    acceptLanguageParam | localeStringParam | messageParam
    "hr-HR"             | "hr_HR"           | "Nije moguće sortirati po podatku s nazivom 'nonExistingProperty'. Naziv ne postoji."
    "en"                | "en"              | "Can't sort by property with name 'nonExistingProperty'. Property name does not exist."
  }
}
