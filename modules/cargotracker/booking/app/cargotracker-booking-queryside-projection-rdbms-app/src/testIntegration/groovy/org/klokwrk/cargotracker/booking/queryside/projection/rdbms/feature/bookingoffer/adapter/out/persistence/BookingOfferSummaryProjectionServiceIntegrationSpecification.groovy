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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.feature.bookingoffer.adapter.out.persistence

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataConstant
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.projection.rdbms.test.base.AbstractProjectionRdbmsIntegrationSpecification
import org.klokwrk.cargotracker.booking.test.support.queryside.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.test.support.queryside.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.lang.groovy.constant.CommonConstants
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
class BookingOfferSummaryProjectionServiceIntegrationSpecification extends AbstractProjectionRdbmsIntegrationSpecification {

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

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerIdentifier = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage =
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, WebMetaDataFixtureBuilder.webMetaData_booking_default().build())

    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdentifier)) {
        size() == 21
        booking_offer_identifier == bookingOfferIdentifier

        customer_identifier == customerIdentifier
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

        inbound_channel_name == WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
        inbound_channel_type == WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
        last_event_sequence_number == 0

        // collections verification
        commodity_type_list == ["DRY"] as Set
      }
    }
  }

  void "should work for event message without metadata"() {
    given:
    Instant startedAt = Instant.now()
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerIdentifier = bookingOfferCreatedEvent.customer.customerId
    CustomerType customerType = bookingOfferCreatedEvent.customer.customerType

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, [:])
    eventBus.publish(genericDomainEventMessage)

    expect:
    new PollingConditions(timeout: 10, initialDelay: 0, delay: 0.1).eventually {
      BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1
      verifyAll(BookingOfferSummarySqlHelper.selectBookingOfferSummaryRecord(groovySql, bookingOfferIdentifier)) {
        size() == 21
        booking_offer_identifier == bookingOfferIdentifier

        customer_identifier == customerIdentifier
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

        inbound_channel_name == CommonConstants.NOT_AVAILABLE
        inbound_channel_type == CommonConstants.NOT_AVAILABLE

        (first_event_recorded_at as Timestamp).toInstant() >= startedAt
        (last_event_recorded_at as Timestamp).toInstant() >= startedAt
        last_event_sequence_number == 0

        // collections verification
        commodity_type_list == ["DRY"] as Set
      }
    }
  }

  void "should execute expected SQL insert statements"() {
    // NOTE: Here we are testing whether our Spring Data JPA repository implementation optimally works when persisting the new entity with the assigned identifier. We want only SQL inserts to be
    //       executed, without any additional and unnecessary SQL selects. For more information, take a look at the article at https://vladmihalcea.com/best-spring-data-jparepository/ and the usage
    //       of io.hypersistence.utils.spring.repository.HibernateRepository in BookingOfferSummaryProjectionJpaRepository.
    given:
    Logger logger = LoggerFactory.getLogger("klokwrk.datasourceproxy.queryLogger") as Logger
    logger.level = Level.DEBUG
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    UUID bookingOfferIdentifier = UUID.fromString(bookingOfferCreatedEvent.bookingOfferId)
    String customerIdentifier = bookingOfferCreatedEvent.customer.customerId

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, [:])

    when:
    eventBus.publish(genericDomainEventMessage)

    then:
    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.1).eventually {
      listAppender.list.size() == 2

      String firstFormattedMessage = listAppender.list[0].formattedMessage
      firstFormattedMessage.contains("insert into booking_offer_summary")
      firstFormattedMessage.contains(bookingOfferIdentifier.toString())
      firstFormattedMessage.contains(customerIdentifier)

      String secondFormattedMessage2 = listAppender.list[1].formattedMessage
      secondFormattedMessage2.contains("insert into booking_offer_summary_commodity_type")
      secondFormattedMessage2.contains(bookingOfferIdentifier.toString())
    }

    cleanup:
    logger.detachAppender(listAppender)
  }
}
