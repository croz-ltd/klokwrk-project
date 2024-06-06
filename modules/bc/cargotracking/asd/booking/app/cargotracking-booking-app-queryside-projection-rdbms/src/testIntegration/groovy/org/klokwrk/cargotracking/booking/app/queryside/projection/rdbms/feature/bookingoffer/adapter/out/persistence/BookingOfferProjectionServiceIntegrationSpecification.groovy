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
package org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.feature.bookingoffer.adapter.out.persistence

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.app.queryside.projection.rdbms.test.base.AbstractProjectionRdbmsIntegrationSpecification
import org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracking.booking.test.support.queryside.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracking.booking.test.support.queryside.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.lib.domain.model.event.BaseEvent
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookingOfferProjectionServiceIntegrationSpecification extends AbstractProjectionRdbmsIntegrationSpecification {

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

  void "booking offer creation - partial - customer"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    List<BaseEvent> bookingOfferCreationEventsSequence = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreation_complete_defaultEventsSequence()
    BookingOfferCreatedEvent bookingOfferCreatedEvent = (bookingOfferCreationEventsSequence[0] as BookingOfferCreatedEvent)
    UUID bookingOfferIdAsUuid = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerId = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    eventBus.publish([
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[0], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 0L),
    ])

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 21
        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 0

        customer_id == customerId
        customer_type == customerType.name()

        origin_location_un_lo_code == null
        origin_location_name == null
        origin_location_country_name == null

        destination_location_un_lo_code == null
        destination_location_name == null
        destination_location_country_name == null

        departure_earliest_time == null
        departure_latest_time == null
        arrival_latest_time == null

        total_commodity_weight == null
        total_commodity_weight_kg == null
        total_container_teu_count == null
        (commodity_type_list as Set).isEmpty()

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }

      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferDetailsRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 8

        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 0

        customer_id == customerId

        (details as String).matches(/.*customerType.*STANDARD.*/)

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }
    }
  }

  void "booking offer creation - partial - customer and routeSpecification"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    List<BaseEvent> bookingOfferCreationEventsSequence = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreation_complete_defaultEventsSequence()
    BookingOfferCreatedEvent bookingOfferCreatedEvent = (bookingOfferCreationEventsSequence[0] as BookingOfferCreatedEvent)
    UUID bookingOfferIdAsUuid = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerId = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    eventBus.publish([
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[0], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 0L),
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[1], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 1L),
    ])

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 21
        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 1

        customer_id == customerId
        customer_type == customerType.name()

        origin_location_un_lo_code == "HRRJK"
        origin_location_name == "Rijeka"
        origin_location_country_name == "Croatia"

        destination_location_un_lo_code == "NLRTM"
        destination_location_name == "Rotterdam"
        destination_location_country_name == "Netherlands"

        (departure_earliest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(1)
        (departure_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(2)
        (arrival_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(3)

        total_commodity_weight == null
        total_commodity_weight_kg == null
        total_container_teu_count == null
        (commodity_type_list as Set).isEmpty()

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }

      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferDetailsRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 8

        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 1

        customer_id == customerId

        (details as String).matches(/.*originLocation.*Rijeka.*destinationLocation.*Rotterdam.*/)

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }
    }
  }

  void "booking offer creation - complete - should work for event messages with metadata"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    List<BaseEvent> bookingOfferCreationEventsSequence = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreation_complete_defaultEventsSequence()
    BookingOfferCreatedEvent bookingOfferCreatedEvent = (bookingOfferCreationEventsSequence[0] as BookingOfferCreatedEvent)
    UUID bookingOfferIdAsUuid = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerId = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    eventBus.publish([
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[0], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 0L),
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[1], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 1L),
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[2], WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 2L)
    ])

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 21
        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 2

        customer_id == customerId
        customer_type == customerType.name()

        origin_location_un_lo_code == "HRRJK"
        origin_location_name == "Rijeka"
        origin_location_country_name == "Croatia"

        destination_location_un_lo_code == "NLRTM"
        destination_location_name == "Rotterdam"
        destination_location_country_name == "Netherlands"

        (departure_earliest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(1)
        (departure_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(2)
        (arrival_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(3)

        total_commodity_weight == "1000 kg"
        total_commodity_weight_kg == 1000
        total_container_teu_count == 1.00G
        commodity_type_list == ["DRY"] as Set

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }

      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferDetailsRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 8

        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 2

        customer_id == customerId

        (details as String).matches(/.*originLocation.*Rijeka.*destinationLocation.*Rotterdam.*/)

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }
    }
  }

  void "booking offer creation - complete - should work for event messages without metadata"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    List<BaseEvent> bookingOfferCreationEventsSequence = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreation_complete_defaultEventsSequence()
    BookingOfferCreatedEvent bookingOfferCreatedEvent = (bookingOfferCreationEventsSequence[0] as BookingOfferCreatedEvent)
    UUID bookingOfferIdAsUuid = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerId = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    when:
    eventBus.publish([
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[0], [:], 0L),
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[1], [:], 1L),
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreationEventsSequence[2], [:], 2L)
    ])

    then:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 21
        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 2

        customer_id == customerId
        customer_type == customerType.name()

        origin_location_un_lo_code == "HRRJK"
        origin_location_name == "Rijeka"
        origin_location_country_name == "Croatia"

        destination_location_un_lo_code == "NLRTM"
        destination_location_name == "Rotterdam"
        destination_location_country_name == "Netherlands"

        (departure_earliest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(1)
        (departure_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(2)
        (arrival_latest_time as Timestamp).toInstant() >= startedAt + Duration.ofHours(3)

        total_commodity_weight == "1000 kg"
        total_commodity_weight_kg == 1000
        total_container_teu_count == 1.00G
        commodity_type_list == ["DRY"] as Set

        inbound_channel_name == CommonConstants.NOT_AVAILABLE
        inbound_channel_type == CommonConstants.NOT_AVAILABLE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }

      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferDetailsRecord(groovySql, bookingOfferIdAsUuid)) {
        size() == 8

        booking_offer_id == bookingOfferIdAsUuid
        last_event_sequence_number == 2

        customer_id == customerId

        (details as String).matches(/.*originLocation.*Rijeka.*destinationLocation.*Rotterdam.*/)

        inbound_channel_name == CommonConstants.NOT_AVAILABLE
        inbound_channel_type == CommonConstants.NOT_AVAILABLE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
      }
    }
  }

  void "booking offer creation - partial - BookingOfferCreatedEvent - should execute expected SQL statements"() {
    // NOTE: Here we are testing whether our Spring Data JPA repository implementations optimally works when persisting the new entity with the assigned identifier. We want only SQL inserts to be
    //       executed, without any additional and unnecessary SQL selects. For more information, take a look at the article at https://vladmihalcea.com/best-spring-data-jparepository/ and the usage
    //       of io.hypersistence.utils.spring.repository.HibernateRepository in BookingOfferSummaryProjectionJpaRepository.
    given:
    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    String bookingOfferId = bookingOfferCreatedEvent.bookingOfferId
    String customerId = bookingOfferCreatedEvent.customer.customerId

    when:
    eventBus.publish(GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, WebMetaDataFixtureBuilder.webMetaData_booking_default().build(), 0L))

    then:
    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.1).eventually {
      listAppender.list.size() == 2

      String matchingMessage1 = listAppender.list.find({ it.formattedMessage.matches(/.*insert into booking_offer_summary \(.*customer_id,.*/) })
      matchingMessage1 != null
      matchingMessage1.contains(bookingOfferId)
      matchingMessage1.contains(customerId)

      String matchingMessage2 = listAppender.list.find({ it.formattedMessage.matches(/.*insert into booking_offer_details \(.*customer_id,.*/) })
      matchingMessage2 != null
      matchingMessage2.contains(bookingOfferId)
      matchingMessage2.contains(customerId)
    }

    cleanup:
    logger.detachAppender(listAppender)
  }
}
