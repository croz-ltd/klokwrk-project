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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortDirection
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
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should work for customized search request with default paging and sorting"() {
    given:
    String webRequestBody = objectMapper.writeValueAsString([
        userIdentifier: "standard-customer@cargotracker.com",
        customerTypeSearchList: [CustomerType.STANDARD, CustomerType.GOLD],
        originLocationName: "Rijeka",
        commodityTotalWeightKgFromIncluding: 5_000,
        commodityTotalWeightKgToIncluding: 50_000
    ])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/booking-offer/booking-offer-summary-search-all")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()

    SortDirection.DESC.name()
    verifyAll(responseContentMap as Map) {
      verifyAll(it.metaData as Map) {
        verifyAll(it.general as Map) {
          size() == 3
        }
        verifyAll(it.http as Map) {
          size() == 2
        }
      }

      verifyAll(it.payload as Map) {
        verifyAll(it.pageInfo as Map) {
          size() == 8
          pageOrdinal == 0
          pageElementsCount >= 2
          first

          verifyAll(it.requestedPageRequirement as Map) {
            size() == 2
          }

          verifyAll((it.requestedSortRequirementList as List)[0] as Map) {
            size() == 2
            propertyName == "lastEventRecordedAt"
            direction == SortDirection.DESC.name()
          }
        }

        verifyAll((it.pageContent as List).first() as Map) {
          customerType == CustomerType.STANDARD.name()
          originLocationName == "Rijeka"
          destinationLocationName == "Los Angeles"
          commodityTotalWeightKg == 15_000
          commodityTotalContainerTeuCount == 1.00G
          lastEventSequenceNumber == 0
        }
      }
    }

    where:
    acceptLanguage | localeString
    "hr-HR"        | "hr_HR"
    "en"           | "en"
  }
}
