package org.klokwrk.cargotracker.booking.queryside.cargosummary.test.base

import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsProjectionAppTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

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
}
