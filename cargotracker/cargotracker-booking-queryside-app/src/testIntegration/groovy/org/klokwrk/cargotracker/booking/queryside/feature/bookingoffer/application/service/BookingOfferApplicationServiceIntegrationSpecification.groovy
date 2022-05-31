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
package org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.service

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryResponse
import org.klokwrk.cargotracker.booking.queryside.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

import javax.sql.DataSource
import java.time.Instant

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferApplicationServiceIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
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
  BookingOfferSummaryQueryPortIn bookingOfferSummaryQueryPortIn

  void "should work for correct request - [locale: #localeParam]"() {
    given:
    Instant startedAt = Instant.now()
    String myBookingOfferIdentifier = publishAndWaitForProjectedBookingOfferCreatedEvent(eventBus, groovySql)

    // Note: "standard-customer@cargotracker.com" corresponds to the customerId.identifier created by publishAndWaitForProjectedBookingOfferCreatedEvent
    BookingOfferSummaryQueryRequest bookingOfferSummaryQueryRequest =
        new BookingOfferSummaryQueryRequest(bookingOfferIdentifier: myBookingOfferIdentifier, userIdentifier: "standard-customer@cargotracker.com")

    OperationRequest<BookingOfferSummaryQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): localeParam]
    )

    when:
    OperationResponse<BookingOfferSummaryQueryResponse> operationResponse = bookingOfferSummaryQueryPortIn.bookingOfferSummaryQuery(operationRequest)

    then:
    verifyAll(operationResponse.payload) {
      propertiesFiltered.size() == 6

      bookingOfferIdentifier == myBookingOfferIdentifier
      originLocation == "HRRJK"
      destinationLocation == "NLRTM"

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

    where:
    localeParam                    | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }

  void "should throw when booking offer summary cannot be found - [locale: #locale]"() {
    given:
    BookingOfferSummaryQueryRequest bookingOfferSummaryQueryRequest =
        new BookingOfferSummaryQueryRequest(bookingOfferIdentifier: UUID.randomUUID(), userIdentifier: "standard-customer@cargotracker.com")

    OperationRequest<BookingOfferSummaryQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferSummaryQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    when:
    bookingOfferSummaryQueryPortIn.bookingOfferSummaryQuery(operationRequest)

    then:
    QueryException queryException = thrown()
    queryException.violationInfo == ViolationInfo.NOT_FOUND

    where:
    locale                         | _
    Locale.forLanguageTag("hr-HR") | _
    Locale.forLanguageTag("en")    | _
  }
}
