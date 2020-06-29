package org.klokwrk.cargotracker.booking.queryside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

@CompileStatic
class RdbmsManagementAppTestcontainersFactory {
  static GenericContainer createAndStartRdbmsManagementApp(Network klokwrkNetwork, PostgreSQLContainer postgresqlServer) {
    String imageVersion = System.getProperty("cargotrackerBookingRdbmsManagementAppDockerImageVersion")
    String containerName = "cargotracker-booking-rdbms-management-app"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer rdbmsManagementApp = new GenericContainer("klokwrkprj/cargotracker-booking-rdbms-management-app:${ imageVersion }")
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

    rdbmsManagementApp.start()

    return rdbmsManagementApp
  }
}
