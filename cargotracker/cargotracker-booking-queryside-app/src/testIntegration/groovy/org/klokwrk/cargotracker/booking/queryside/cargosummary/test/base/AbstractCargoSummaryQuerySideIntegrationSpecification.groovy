package org.klokwrk.cargotracker.booking.queryside.cargosummary.test.base

import groovy.sql.Sql
import org.awaitility.Awaitility
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.CargoBookedEventFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.axon.GenericDomainEventMessageFactory
import org.klokwrk.cargotracker.booking.queryside.test.feature.cargosummary.sql.CargoSummarySqlHelper
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

abstract class AbstractCargoSummaryQuerySideIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().id("klokwrk-network-${ UUID.randomUUID() }").build()

    postgresqlServer = PostgreSqlTestcontainersFactory.createAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.createAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.createAndStartAxonServer(klokwrkNetwork)
    RdbmsProjectionAppTestcontainersFactory.createAndStartRdbmsProjectionApp(klokwrkNetwork, axonServer, postgresqlServer)
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.containerIpAddress
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)
    registry.add("axon.axonserver.servers", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })

    registry.add("spring.datasource.url", { postgresqlServer.jdbcUrl })
    registry.add("spring.datasource.username", { postgresqlServer.username })
    registry.add("spring.datasource.password", { postgresqlServer.password })
  }

  static String publishAndWaitForProjectedCargoBookedEvent(EventBus eventBus, Sql groovySql, CargoBookedEvent cargoBookedEvent = CargoBookedEventFixtures.eventValidConnectedViaRail()) {
    Long startingCargoSummaryRecordsCount = CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql)
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier

    GenericDomainEventMessage<CargoBookedEvent> genericDomainEventMessage = GenericDomainEventMessageFactory.createEventMessage(cargoBookedEvent, WebMetaDataFixtures.metaDataMapForWebBookingChannel())
    eventBus.publish(genericDomainEventMessage)

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({ CargoSummarySqlHelper.selectCurrentCargoSummaryRecordsCount(groovySql) == startingCargoSummaryRecordsCount + 1 })

    return aggregateIdentifier
  }
}
