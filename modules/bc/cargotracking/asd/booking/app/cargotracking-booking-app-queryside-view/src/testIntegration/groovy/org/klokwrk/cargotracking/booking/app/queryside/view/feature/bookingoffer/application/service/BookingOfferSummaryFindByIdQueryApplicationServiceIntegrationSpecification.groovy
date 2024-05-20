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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryPortIn
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryRequest
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryResponse
import org.klokwrk.cargotracking.booking.app.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummaryFindByIdQueryApplicationServiceIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
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
  BookingOfferSummaryFindByIdQueryPortIn bookingOfferSummaryFindByIdQueryPortIn

  void "should work for correct request"() {
    given:
    Instant startedAt = Instant.now()
    String myBookingOfferId = publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql)

    // Note: "standard-customer@cargotracking.com" corresponds to the customerId.identifier created by publishAndWaitForProjectedBookingOfferCreatedEvent
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferSummaryFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindByIdQueryResponse> operationResponse = bookingOfferSummaryFindByIdQueryPortIn.bookingOfferSummaryFindByIdQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload) {
      propertiesFiltered.size() == 17

      bookingOfferId == myBookingOfferId

      customerType == CustomerType.STANDARD

      originLocationUnLoCode == "HRRJK"
      originLocationName == "Rijeka"
      originLocationCountryName == "Croatia"

      destinationLocationUnLoCode == "NLRTM"
      destinationLocationName == "Rotterdam"
      destinationLocationCountryName == "Netherlands"

      departureEarliestTime >= startedAt + Duration.ofHours(1)
      departureLatestTime >= startedAt + Duration.ofHours(2)
      arrivalLatestTime >= startedAt + Duration.ofHours(3)

      commodityTypes == [CommodityType.DRY].toSet()
      totalCommodityWeight == 1000.kg
      totalContainerTeuCount == 1.00G

      firstEventRecordedAt >= startedAt
      lastEventRecordedAt >= startedAt
      lastEventSequenceNumber == 0
    }

    verifyAll(operationResponse.metaData) {
      general.timestamp
      general.severity == Severity.INFO.name().toLowerCase()
      general.locale == null
      violation == null
    }
  }

  void "should throw when booking offer summary cannot be found"() {
    given:
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: UUID.randomUUID(), userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferSummaryFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferSummaryFindByIdQueryPortIn.bookingOfferSummaryFindByIdQuery(operationRequest)

    then:
    QueryException queryException = thrown()
    queryException.violationInfo == ViolationInfo.NOT_FOUND
  }

  void "should execute expected single select statement"() {
    given:
    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    String myBookingOfferId = publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql)

    // Note: "standard-customer@cargotracking.com" corresponds to the customerId.identifier created by publishAndWaitForProjectedBookingOfferCreatedEvent
    BookingOfferSummaryFindByIdQueryRequest bookingOfferSummaryFindByIdQueryRequest =
        new BookingOfferSummaryFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferSummaryFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferSummaryFindByIdQueryResponse> operationResponse = bookingOfferSummaryFindByIdQueryPortIn.bookingOfferSummaryFindByIdQuery(operationRequest)

    then:
    operationResponse.payload.bookingOfferId

    List<ILoggingEvent> filteredLoggingEventList =
        listAppender.list.dropWhile({ ILoggingEvent iLoggingEvent -> iLoggingEvent.formattedMessage.contains("SELECT count(*) as recordsCount from booking_offer_summary") })
    filteredLoggingEventList.size() == 1

    String formattedMessage = filteredLoggingEventList.first().formattedMessage
    formattedMessage.matches(/.*select.*from booking_offer_summary.*left join booking_offer_summary_commodity_type.*/)

    cleanup:
    logger.detachAppender(listAppender)
  }
}
