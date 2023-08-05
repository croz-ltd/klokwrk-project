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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.booking.test.support.queryside.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationCode
import org.klokwrk.cargotracking.lib.boundary.query.api.paging.PageRequirement
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortDirection
import org.klokwrk.cargotracking.lib.boundary.query.api.sorting.SortRequirement
import org.slf4j.LoggerFactory
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared

import jakarta.validation.ConstraintViolationException
import javax.sql.DataSource

@SuppressWarnings("GroovyAccessibility")
@EnableSharedInjection
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

  @Shared
  @Autowired
  EventBus eventBus

  @Shared
  @Autowired
  Sql groovySql

  @Autowired
  BookingOfferSummaryFindAllQueryPortIn bookingOfferSummaryFindAllQueryPortIn

  @Shared
  Integer initialBookingOfferSummaryRecordsCount = null

  void setupSpec() {
    String customerId = CustomerFixtureBuilder.customer_standard().build().customerId.identifier
    initialBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount_forCustomerId(groovySql, customerId)
    5.times { publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql) }
  }

  void "should work for correct request with default paging and sorting"() {
    given:
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(userId: "standard-customer@cargotracker.com")

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8

      pageOrdinal == 0
      pageElementsCount == Math.min(initialBookingOfferSummaryRecordsCount + 5, PageRequirement.PAGE_REQUIREMENT_SIZE_DEFAULT)
      first
      totalElementsCount == initialBookingOfferSummaryRecordsCount + 5

      requestedPageRequirement == PageRequirement.PAGE_REQUIREMENT_INSTANCE_DEFAULT
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.DESC)]
    }

    operationResponse.payload.pageContent.size() == Math.min(initialBookingOfferSummaryRecordsCount + 5, PageRequirement.PAGE_REQUIREMENT_SIZE_DEFAULT)

    verifyAll(operationResponse.payload.pageContent[0]) {
      propertiesFiltered.size() == 17

      customerType == CustomerType.STANDARD
      originLocationName == "Rijeka"
      destinationLocationName == "Rotterdam"
      totalCommodityWeight == 1000.kg
      totalContainerTeuCount == 1.00G
      lastEventSequenceNumber == 0
    }
  }

  void "should work for correct request with explicit paging and sorting"() {
    given:
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userId: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload.pageInfo) {
      propertiesFiltered.size() == 8

      pageOrdinal == 0
      pageElementsCount == 3
      first
      totalElementsCount == initialBookingOfferSummaryRecordsCount + 5

      requestedPageRequirement == new PageRequirement(ordinal: 0, size: 3)
      requestedSortRequirementList == [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    }

    operationResponse.payload.pageContent.size() as Long == 3
  }

  void "should fail for invalid sort requirements list"() {
    given:
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userId: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: sortRequirementListParam
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    ConstraintViolationException constraintViolationException = thrown()
    constraintViolationException.message.startsWith("sortRequirementList: ")

    where:
    sortRequirementListParam | _
    null                     | _
    []                       | _
    [null]                   | _
  }

  void "should fail for invalid property name in sort requirements"() {
    given:
    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userId: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: [new SortRequirement(propertyName: "nonExistingProperty", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    QueryException queryException = thrown()

    queryException.message == "Bad Request"
    verifyAll(queryException.violationInfo, {
      severity == Severity.WARNING
      violationCode.code == ViolationCode.BAD_REQUEST.code
      violationCode.codeMessage == ViolationCode.BAD_REQUEST.codeMessage
      violationCode.resolvableMessageKey == "badRequest.query.sorting.invalidProperty"
      violationCode.resolvableMessageParameters == ["nonExistingProperty"]
    })
  }

  void "should execute expected select statements when first page includes all available entities"() {
    given:
    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userId: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 100),
        sortRequirementList: [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    operationResponse.payload.pageInfo.totalElementsCount == initialBookingOfferSummaryRecordsCount + 5
    listAppender.list.size() == 2

    String firstFormattedMessage = listAppender.list[0].formattedMessage
    firstFormattedMessage.matches(/.*select.*booking_offer_id from booking_offer_summary \w* where.*offset.*rows fetch first.*rows only.*/)

    String secondFormattedMessage = listAppender.list[1].formattedMessage
    secondFormattedMessage.matches(/.*select.*from booking_offer_summary \w* left join booking_offer_summary_commodity_type.*where.*booking_offer_id in \(.*/)

    cleanup:
    logger.detachAppender(listAppender)
  }

  void "should execute expected select statements when first page includes subset of all available entities"() {
    given:
    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    BookingOfferSummaryFindAllQueryRequest bookingOfferSummaryFindAllQueryRequest = new BookingOfferSummaryFindAllQueryRequest(
        userId: "standard-customer@cargotracker.com",
        pageRequirement: new PageRequirement(ordinal: 0, size: 3),
        sortRequirementList: [new SortRequirement(propertyName: "lastEventRecordedAt", direction: SortDirection.ASC)]
    )

    OperationRequest<BookingOfferSummaryFindAllQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindAllQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> operationResponse = bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(operationRequest)

    then:
    operationResponse.payload.pageInfo.totalElementsCount == initialBookingOfferSummaryRecordsCount + 5
    listAppender.list.size() == 3

    String firstFormattedMessage = listAppender.list[0].formattedMessage
    firstFormattedMessage.matches(/.*select.*booking_offer_id from booking_offer_summary \w* where.*offset.*rows fetch first.*rows only.*/)

    String secondFormattedMessage = listAppender.list[1].formattedMessage
    secondFormattedMessage.matches(/.*select count\(\w*.booking_offer_id\).*from booking_offer_summary \w* where.*/)

    String thirdFormattedMessage = listAppender.list[2].formattedMessage
    thirdFormattedMessage.matches(/.*select.*from booking_offer_summary \w* left join booking_offer_summary_commodity_type.*where.*booking_offer_id in \(.*/)

    cleanup:
    logger.detachAppender(listAppender)
  }
}
