package org.klokwrk.cargotracker.booking.test.component.test.base

import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.CommandSideAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.QuerySideAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsProjectionAppTestcontainersFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

class AbstractComponentIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork
  static GenericContainer commandSideApp
  static GenericContainer querySideApp

  static {
    klokwrkNetwork = Network.builder().id("klokwrk-network-${ UUID.randomUUID() }").build()

    postgresqlServer = PostgreSqlTestcontainersFactory.createAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.createAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.createAndStartAxonServer(klokwrkNetwork)

    commandSideApp = CommandSideAppTestcontainersFactory.createAndStartCommandSideApp(klokwrkNetwork, axonServer)
    RdbmsProjectionAppTestcontainersFactory.createAndStartRdbmsProjectionApp(klokwrkNetwork, axonServer, postgresqlServer)
    querySideApp = QuerySideAppTestcontainersFactory.createAndStartQuerySideApp(klokwrkNetwork, axonServer, postgresqlServer)
  }
}
