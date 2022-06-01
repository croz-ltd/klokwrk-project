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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.bookingoffer.adapter.out.persistence

import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.bookingoffer.BookingOfferCreatedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.test.base.AbstractRdbmsProjectionIntegrationSpecification
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferSummaryProjectionServiceIntegrationSpecification extends AbstractRdbmsProjectionIntegrationSpecification {

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

  void "should work for event message with metadata"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtures.eventValidRouteSpecification()
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId.identifier)
    String customerIdentifier = bookingOfferCreatedEvent.customer.customerId.identifier
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage =
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())

    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdentifier)) {
        size() == 10
        booking_offer_identifier == bookingOfferIdentifier

        customer_identifier == customerIdentifier
        customer_type == customerType.name()

        origin_location_un_lo_code == "HRRJK"
        destination_location_un_lo_code == "NLRTM"

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
        last_event_sequence_number == 0
      }
    }
  }

  void "should work for event message without metadata"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtures.eventValidRouteSpecification()
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId.identifier)
    String customerIdentifier = bookingOfferCreatedEvent.customer.customerId.identifier
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, [:])
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdentifier)) {
        size() == 10
        booking_offer_identifier == bookingOfferIdentifier

        customer_identifier == customerIdentifier
        customer_type == customerType.name()

        origin_location_un_lo_code == "HRRJK"
        destination_location_un_lo_code == "NLRTM"

        inbound_channel_name == CommonConstants.NOT_AVAILABLE
        inbound_channel_type == CommonConstants.NOT_AVAILABLE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
        last_event_sequence_number == 0
      }
    }
  }
}
