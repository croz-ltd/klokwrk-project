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
import org.klokwrk.cargotracker.booking.queryside.test.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracker.lib.boundary.query.api.sorting.SortRequirement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

import javax.sql.DataSource

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummaryFindAllQueryApplicationServiceIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
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
  BookingOfferSummaryFindAllQueryPortIn bookingOfferSummaryFindAllQueryPortIn

  void "should work for correct request with default paging and sorting"() {
    given:
    Long initialBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)
    5.times { publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql) }

    // Note: "standard-customer@cargotracker.com" corresponds to the customerId.identifier created by publishAndWaitForProjectedBookingOfferCreatedEvent
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(userIdentifier: "standard-customer@cargotracker.com")

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8

      pageOrdinal == 0
      pageElementsCount == initialBookingOfferSummaryRecordsCount + 5
      first
      last
      totalPagesCount == 1
      totalElementsCount == initialBookingOfferSummaryRecordsCount + 5

      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    operationResponse.payload.pageContent.size() as Long == initialBookingOfferSummaryRecordsCount + 5

    verifyAll(operationResponse.payload.pageContent[0]) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "Rotterdam"
      commodityTotalWeightKg == 1000
      commodityTotalContainerTeuCount == 1.00G
      lastEventSequenceNumber == 0
    }

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }

  void "should work for correct request with explicit paging and sorting"() {
    given:
    Long initialBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)
    5.times { publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql) }

    // Note: "standard-customer@cargotracker.com" corresponds to the customerId.identifier created by publishAndWaitForProjectedBookingOfferCreatedEvent
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8

      pageOrdinal == 0
      pageElementsCount == 3
      first
      !last
      totalPagesCount >= 2
      totalElementsCount == initialBookingOfferSummaryRecordsCount + 5

      requestedPageRequirement == new PageRequirement(ordinal: 0, size: 3)
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    }

    operationResponse.payload.pageContent.size() as Long == 3

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }

  void "should fail for invalid property name in sort requirements"() {
    given:
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: [new SortRequirement(propertyName: "nonExistingProperty", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    QueryException queryException = thrown()

    queryException.message == "Internal Server Error"
    verifyAll(queryException.violationInfo, {
      severity == Severity.WARNING
      violationCode.code == ViolationCode.BAD_REQUEST.code
      violationCode.codeMessage == ViolationCode.BAD_REQUEST.codeMessage
      violationCode.resolvableMessageKey == "badRequest.query.sorting.invalidProperty"
      violationCode.resolvableMessageParameters == ["nonExistingProperty"]
    })

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }
}
