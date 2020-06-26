package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.test.base

import com.github.dockerjava.api.command.CreateContainerCmd
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

class AbstractCargoSummaryIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().id("klokwrk-network-${ UUID.randomUUID() }").build()

    postgresqlServer = createAndStartPostgresqlServer(klokwrkNetwork)
    createAndStartCargotrackerBookingRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.createAndStartAxonServer(klokwrkNetwork)
  }

  static PostgreSQLContainer createAndStartPostgresqlServer(Network klokwrkNetwork) {
    // TODO dmurat: manage image version externally
    String imageVersion = "12.3"
    String containerName = "klokwrk-project-postgres"
    String containerNameSuffix = UUID.randomUUID()

    PostgreSQLContainer postgresqlServer = new PostgreSQLContainer<>("postgres:${ imageVersion }")
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv(["TZ": "Europe/Zagreb"])
        .withDatabaseName("cargotracker_booking_query_database")
        .withUsername("cargotracker")
        .withPassword("cargotracker")
        .withNetwork(klokwrkNetwork)

    postgresqlServer.start()

    return postgresqlServer
  }

  static GenericContainer createAndStartCargotrackerBookingRdbmsManagementApp(Network klokwrkNetwork, PostgreSQLContainer postgresqlServer) {
    // TODO dmurat: manage image version externally
    String imageVersion = "0.0.2-SNAPSHOT"
    String containerName = "cargotracker-booking-rdbms-management-app"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer cargotrackerBookingRdbmsManagementApp = new GenericContainer("klokwrkprj/cargotracker-booking-rdbms-management-app:${ imageVersion }")
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv([
            "TZ": "Europe/Zagreb",
            "CARGOTRACKER_POSTGRES_HOSTNAME": "${ postgresqlServer.containerInfo.name - "/" }".toString(),
            "CARGOTRACKER_POSTGRES_PORT": "5432",
            "CARGOTRACKER_POSTGRES_USERNAME": "cargotracker",
            "CARGOTRACKER_POSTGRES_PASSWORD": "cargotracker"
        ])
        .withNetwork(klokwrkNetwork)
        .waitingFor(Wait.forLogMessage(/.*Successfully applied.*migration.*to schema "public".*/, 1))

    cargotrackerBookingRdbmsManagementApp.start()

    return cargotrackerBookingRdbmsManagementApp
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
