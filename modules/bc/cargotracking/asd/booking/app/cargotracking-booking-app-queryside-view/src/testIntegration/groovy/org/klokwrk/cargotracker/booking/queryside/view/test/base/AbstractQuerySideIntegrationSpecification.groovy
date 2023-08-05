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
package org.klokwrk.cargotracker.booking.queryside.view.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import groovy.sql.Sql
import org.awaitility.Awaitility
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracking.booking.test.support.queryside.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracking.booking.test.support.queryside.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracking.booking.test.support.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.QuerySideProjectionRdbmsAppTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import java.time.Duration

import static org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder.cargo_airCooled
import static org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder.cargo_chilled
import static org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder.cargo_dry
import static org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder.cargo_frozen
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_hamburgToLosAngeles
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_hamburgToRotterdam
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToHamburg
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToLosAngeles
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToNewYork
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam
import static org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder.routeSpecification_rotterdamToNewYork

abstract class AbstractQuerySideIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)
    QuerySideProjectionRdbmsAppTestcontainersFactory.makeAndStartQuerySideProjectionRdbmsApp(klokwrkNetwork, axonServer, postgresqlServer)
  }

  @DynamicPropertySource
  static void configureDynamicTestcontainersProperties(DynamicPropertyRegistry registry) {
    String axonContainerHost = axonServer.host
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)
    registry.add("CARGOTRACKER_AXON_SERVER_HOSTNAME", { axonContainerHost })
    registry.add("CARGOTRACKER_AXON_SERVER_PORT_GRPC", { "${ axonContainerGrpcPort }" })

    String postgresqlServerHost = postgresqlServer.host
    Integer postgresqlServerPort = postgresqlServer.getMappedPort(5432)
    registry.add("CARGOTRACKER_POSTGRES_HOSTNAME", { "${ postgresqlServerHost }" })
    registry.add("CARGOTRACKER_POSTGRES_PORT", { "${ postgresqlServerPort }" })
  }

  static String publishAndWaitForProjectedBookingOfferCreatedEvent(
      EventBus eventBus, Sql groovySql, BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build())
  {
    Long startingBookingOfferSummaryRecordsCount = BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql)
    String bookingOfferId = bookingOfferCreatedEvent.bookingOfferId

    GenericDomainEventMessage<BookingOfferCreatedEvent> genericDomainEventMessage =
        GenericDomainEventMessageFactory.makeEventMessage(bookingOfferCreatedEvent, WebMetaDataFixtureBuilder.webMetaData_booking_default().build())

    eventBus.publish(genericDomainEventMessage)

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({ BookingOfferSummarySqlHelper.selectCurrentBookingOfferSummaryRecordsCount(groovySql) == startingBookingOfferSummaryRecordsCount + 1 })

    return bookingOfferId
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  static List<BookingOfferCreatedEvent> makeForSearch_pastBookingOfferCreatedEvents(Integer numOfRepetitions = 1) {
    List<BookingOfferCreatedEvent> bookingOfferCreatedEventList = []

    numOfRepetitions.times {
      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToRotterdam().build())
          .cargos([cargo_dry().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToHamburg().build())
          .cargos([cargo_airCooled().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToLosAngeles().build())
          .cargos([cargo_chilled().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToNewYork().build())
          .cargos([cargo_frozen().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_hamburgToLosAngeles().build())
          .cargos([cargo_airCooled().commodityWeight(70000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rotterdamToNewYork().build())
          .cargos([cargo_dry().commodityWeight(40000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToHamburg().build())
          .cargos([cargo_dry().commodityWeight(1000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToLosAngeles().build())
          .cargos([cargo_dry().commodityWeight(15000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_rijekaToNewYork().build())
          .cargos([cargo_dry().commodityWeight(100000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()

      bookingOfferCreatedEventList << BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default()
          .routeSpecification(routeSpecification_hamburgToRotterdam().build())
          .cargos([cargo_dry().commodityWeight(45000.kg).maxAllowedWeightPerContainer(20615.kg).build()])
          .build()
    }

    return bookingOfferCreatedEventList
  }
}
