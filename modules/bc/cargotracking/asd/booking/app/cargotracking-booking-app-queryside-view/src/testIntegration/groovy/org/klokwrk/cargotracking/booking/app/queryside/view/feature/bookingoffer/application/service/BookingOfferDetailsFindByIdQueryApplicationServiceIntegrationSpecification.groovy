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
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryPortIn
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryRequest
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryResponse
import org.klokwrk.cargotracking.booking.app.queryside.view.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.metadata.constant.MetaDataConstant
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

import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferDetailsFindByIdQueryResponseContentPayloadAssertion.assertResponseHasPayloadThat
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestProjectionHelpers.waitProjectionBookingOfferSummary_forCompleteBookingOfferCreation
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestProjectionHelpers.waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.test.util.BookingOfferQueryTestProjectionHelpers.waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomerAndRouteSpecification
import static org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat
import static org.klokwrk.lib.xlang.groovy.base.misc.InstantUtils.roundUpInstantToTheHour

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferDetailsFindByIdQueryApplicationServiceIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
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
  BookingOfferDetailsFindByIdQueryPortIn bookingOfferDetailsFindByIdQueryPortIn

  @Autowired
  ObjectMapper objectMapper

  void "should work for correct request - partial booking offer exists - customer"() {
    given:
    Instant startedAt = Instant.now() - Duration.ofMillis(1)
    String myBookingOfferId = waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomer(eventBus, groovySql)

    BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest =
        new BookingOfferDetailsFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferDetailsFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferDetailsFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferDetailsFindByIdQueryResponse> operationResponse = bookingOfferDetailsFindByIdQueryPortIn.bookingOfferDetailsFindByIdQuery(operationRequest)
    Map operationResponseMap = objectMapper.readValue(objectMapper.writeValueAsString(operationResponse), Map)

    then:
    assertResponseHasMetaDataThat(operationResponseMap) {
      isSuccessful_asReturnedFromFacadeApi()
    }

    assertResponseHasPayloadThat(operationResponseMap) {
      isSuccessful_partialBookingOffer_customer()

      hasBookingOfferId(myBookingOfferId)
      hasCustomerTypeOfStandard()
      hasEventMetadataOfTheFirstEventWithCorrectTiming(startedAt)
    }
  }

  void "should work for correct request - partial booking offer exists - customer and routeSpecification"() {
    given:
    Instant startedAt = Instant.now() - Duration.ofMillis(1)
    String myBookingOfferId = waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomerAndRouteSpecification(eventBus, groovySql)

    BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest =
        new BookingOfferDetailsFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferDetailsFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferDetailsFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferDetailsFindByIdQueryResponse> operationResponse = bookingOfferDetailsFindByIdQueryPortIn.bookingOfferDetailsFindByIdQuery(operationRequest)
    Map operationResponseMap = objectMapper.readValue(objectMapper.writeValueAsString(operationResponse), Map)

    then:
    assertResponseHasMetaDataThat(operationResponseMap) {
      isSuccessful_asReturnedFromFacadeApi()
    }

    assertResponseHasPayloadThat(operationResponseMap) {
      isSuccessful_partialBookingOffer_customerAndRouteSpecification()

      hasBookingOfferId(myBookingOfferId)
      hasCustomerTypeOfStandard()

      hasRouteSpecificationThat {
        hasCreationTimeGreaterThan(startedAt)
        hasDepartureEarliestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(1)))
        hasDepartureLatestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(2)))
        hasArrivalLatestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(3)))
        hasOriginLocationOfRijeka()
        hasDestinationLocationOfRotterdam()
      }

      hasEventMetadataOfMultipleEventsWithCorrectTiming(startedAt)
    }
  }

  void "should work for correct request - complete booking offer exists"() {
    given:
    Instant startedAt = Instant.now() - Duration.ofMillis(1)
    String myBookingOfferId = waitProjectionBookingOfferSummary_forCompleteBookingOfferCreation(eventBus, groovySql)

    BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest =
        new BookingOfferDetailsFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferDetailsFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferDetailsFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferDetailsFindByIdQueryResponse> operationResponse = bookingOfferDetailsFindByIdQueryPortIn.bookingOfferDetailsFindByIdQuery(operationRequest)
    Map operationResponseMap = objectMapper.readValue(objectMapper.writeValueAsString(operationResponse), Map)

    then:
    assertResponseHasMetaDataThat(operationResponseMap) {
      isSuccessful_asReturnedFromFacadeApi()
    }

    assertResponseHasPayloadThat(operationResponseMap) {
      isSuccessful_completeBookingOffer()

      hasBookingOfferId(myBookingOfferId)
      hasCustomerTypeOfStandard()
      hasTotalCommodityWeight(1000.kg)
      hasTotalContainerTeuCount(1.0G)
      hasEventMetadataOfMultipleEventsWithCorrectTiming(startedAt)

      hasCargosWithFirstCargoThat {
        isDryDefaultCargo()
        hasMaxAllowedWeightPerContainer(21700.kg)
      }

      hasRouteSpecificationThat {
        hasCreationTimeGreaterThan(startedAt)
        hasDepartureEarliestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(1)))
        hasDepartureLatestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(2)))
        hasArrivalLatestTime(roundUpInstantToTheHour(startedAt + Duration.ofHours(3)))
        hasOriginLocationOfRijeka()
        hasDestinationLocationOfRotterdam()
      }
    }
  }

  void "should throw when booking offer details cannot be found"() {
    given:
    BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest =
        new BookingOfferDetailsFindByIdQueryRequest(bookingOfferId: UUID.randomUUID(), userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferDetailsFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferDetailsFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    bookingOfferDetailsFindByIdQueryPortIn.bookingOfferDetailsFindByIdQuery(operationRequest)

    then:
    QueryException queryException = thrown()
    queryException.violationInfo == ViolationInfo.NOT_FOUND
  }

  void "should execute expected single select statement"() {
    given:
    String myBookingOfferId = waitProjectionBookingOfferSummary_forCompleteBookingOfferCreation(eventBus, groovySql)

    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest =
        new BookingOfferDetailsFindByIdQueryRequest(bookingOfferId: myBookingOfferId, userId: "standard-customer@cargotracking.com")

    OperationRequest<BookingOfferDetailsFindByIdQueryRequest> operationRequest = new OperationRequest(
        payload: bookingOfferDetailsFindByIdQueryRequest,
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): Locale.forLanguageTag("en")]
    )

    when:
    OperationResponse<BookingOfferDetailsFindByIdQueryResponse> operationResponse = bookingOfferDetailsFindByIdQueryPortIn.bookingOfferDetailsFindByIdQuery(operationRequest)

    then:
    operationResponse.payload.bookingOfferId
    String formattedMessage = listAppender.list.first().formattedMessage
    formattedMessage.matches(/.*select.*from booking_offer_details.*where.*booking_offer_id=.*and.*customer_id=.*/)

    cleanup:
    logger.detachAppender(listAppender)
  }
}
