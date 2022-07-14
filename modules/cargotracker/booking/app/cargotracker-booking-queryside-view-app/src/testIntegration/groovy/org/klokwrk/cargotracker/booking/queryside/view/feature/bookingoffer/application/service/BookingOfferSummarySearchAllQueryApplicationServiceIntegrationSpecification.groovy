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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.service

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortRequirement
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared

import javax.sql.DataSource

@SuppressWarnings("GroovyAccessibility")
@EnableSharedInjection
@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummarySearchAllQueryApplicationServiceIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
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
  BookingOfferSummarySearchAllQueryPortIn bookingOfferSummarySearchAllQueryPortIn

  void setup() {
    makeForSearch_pastBookingOfferCreatedEvents().each {
      publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql, it)
    }
  }

  void "should work for minimal search request with default paging and sorting"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(userIdentifier: "standard-customer@cargotracker.com"),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 5
      first
      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Hamburg"
      destinationLocationName == "Rotterdam"
      commodityTotalWeightKg == 45_000
      commodityTotalContainerTeuCount == 3.00G
      lastEventSequenceNumber == 0
    }

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }

  void "should work for customized search request with default paging and sorting"() {
    given:
    OperationRequest<BookingOfferSummarySearchAllQueryRequest> operationRequest = new OperationRequest(
        payload: new BookingOfferSummarySearchAllQueryRequest(
            userIdentifier: "standard-customer@cargotracker.com",
            customerTypeSearchList: [CustomerType.STANDARD, CustomerType.GOLD],
            originLocationName: "Rijeka",
            commodityTotalWeightKgFromIncluding: 5_000,
            commodityTotalWeightKgToIncluding: 50_000
        ),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    OperationResponse<BookingOfferSummarySearchAllQueryResponse> operationResponse = bookingOfferSummarySearchAllQueryPortIn.bookingOfferSummarySearchAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8
      pageOrdinal == 0
      pageElementsCount >= 2
      first
      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    verifyAll(operationResponse.payload.pageContent.first()) { // first is an equivalent of lastEventRecorded DESC
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "Los Angeles"
      commodityTotalWeightKg == 15_000
      commodityTotalContainerTeuCount == 1.00G
      lastEventSequenceNumber == 0
    }

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }
}