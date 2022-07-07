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
package org.klokwrk.cargotracker.booking.queryside.view.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import groovy.sql.Sql
import org.awaitility.Awaitility
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.bookingoffer.BookingOfferCreatedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilities
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.booking.out.customer.adapter.InMemoryCustomerRegistryService
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.QuerySideProjectionRdbmsAppTestcontainersFactory
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import java.time.Duration
import java.time.Instant

@SuppressWarnings("CodeNarc.AbcMetric")
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

  @SuppressWarnings(["CodeNarc.MethodSize", "CodeNarc.AbcMetric"])
  static List<BookingOfferCreatedEvent> makeForSearch_pastBookingOfferCreatedEvents(Integer numOfRepetitions = 1) {
    List<BookingOfferCreatedEvent> bookingOfferCreatedEventList = []

    numOfRepetitions.times {
      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("standard-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 30_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(30_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 2.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("gold-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("USLAX", "Los Angeles", "The United States of America", "1--45---", "3344N 11816W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, CommodityInfo.make(CommodityType.AIR_COOLED, 70_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(70_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 4.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("platinum-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("USNYC", "New York", "The United States of America", "12345---", "4042N 07400W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 40_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(40_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 2.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("standard-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 1_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(1_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 1.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("standard-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("USLAX", "Los Angeles", "The United States of America", "1--45---", "3344N 11816W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 15_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(15_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 1.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("standard-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("HRRJK", "Rijeka", "Croatia", "1234----", "4520N 01424E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("USNYC", "New York", "The United States of America", "12345---", "4042N 07400W", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 100_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(100_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 5.00G
      )

      bookingOfferCreatedEventList << new BookingOfferCreatedEvent(
          customer: InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.get("standard-customer@cargotracker.com"),
          bookingOfferId: BookingOfferId.make(CombUuidShortPrefixUtils.makeCombShortPrefix().toString()),
          routeSpecification: RouteSpecification.make(
              Location.make("DEHAM", "Hamburg", "Germany", "12345---", "5331N 00956E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Location.make("NLRTM", "Rotterdam", "Netherlands", "12345---", "5155N 00430E", PortCapabilities.SEA_CONTAINER_PORT_CAPABILITIES),
              Instant.now() + Duration.ofHours(1),
              Instant.now() + Duration.ofHours(2),
              Instant.now() + Duration.ofHours(3),
              ),
          commodity: Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 45_000), Quantities.getQuantity(20_615, Units.KILOGRAM)),
          bookingTotalCommodityWeight: Quantities.getQuantity(45_000, Units.KILOGRAM),
          bookingTotalContainerTeuCount: 3.00G
      )
    }

    return bookingOfferCreatedEventList
  }
}
