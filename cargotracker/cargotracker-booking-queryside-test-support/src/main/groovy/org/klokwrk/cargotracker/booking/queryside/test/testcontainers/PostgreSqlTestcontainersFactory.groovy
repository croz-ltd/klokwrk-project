package org.klokwrk.cargotracker.booking.queryside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer

@CompileStatic
class PostgreSqlTestcontainersFactory {
  static PostgreSQLContainer createAndStartPostgreSqlServer(Network klokwrkNetwork) {
    // TODO dmurat: manage image version externally
    String imageVersion = "12.3"
    String containerName = "klokwrk-project-postgres"
    String containerNameSuffix = UUID.randomUUID()

    PostgreSQLContainer postgresqlServer = new PostgreSQLContainer("postgres:${ imageVersion }")
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv(["TZ": "Europe/Zagreb"]) as PostgreSQLContainer

    postgresqlServer
        .withDatabaseName("cargotracker_booking_query_database")
        .withUsername("cargotracker")
        .withPassword("cargotracker")
        .withNetwork(klokwrkNetwork)

    postgresqlServer.start()

    return postgresqlServer
  }
}
