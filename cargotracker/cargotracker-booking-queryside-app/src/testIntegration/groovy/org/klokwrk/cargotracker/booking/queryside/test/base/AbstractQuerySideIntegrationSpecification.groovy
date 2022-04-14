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
package org.klokwrk.cargotracker.booking.queryside.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import groovy.sql.Sql
import org.awaitility.Awaitility
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.bookingoffer.BookingOfferCreatedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsProjectionAppTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import java.time.Duration

abstract class AbstractQuerySideIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)
    RdbmsProjectionAppTestcontainersFactory.makeAndStartRdbmsProjectionApp(klokwrkNetwork, axonServer, postgresqlServer)
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.host
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)
    registry.add("axon.axonserver.servers", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })

    registry.add("spring.datasource.url", { postgresqlServer.jdbcUrl })
    registry.add("spring.datasource.username", { postgresqlServer.username })
    registry.add("spring.datasource.password", { postgresqlServer.password })
  }

  static String publishAndWaitForProjectedBookingOfferCreatedEvent(
      EventBus eventBus, Sql groovySql, BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtures.eventValidRouteSpecification())
  {
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)
    String bookingOfferIdentifier = bookingOfferCreatedEvent.bookingOfferId.identifier

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage =
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())

    eventBus.publish(genericDomainEventMessage)

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({ BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1 })

    return bookingOfferIdentifier
  }
}
