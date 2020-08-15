package org.klokwrk.cargotracker.booking.queryside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting cargotracker-booking-queryside-rdbms-projection-app in Testcontainers.
 */
@CompileStatic
class RdbmsProjectionAppTestcontainersFactory {
  /**
   * Creates and start cargotracker-booking-queryside-rdbms-projection-app in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: <code>cargotracker-booking-queryside-rdbms-projection-app</code>.</li>
   *   <li>Exposed internal ports: 8082.</li>
   *   <li>Container time zone: <code>Europe/Zagreb</code>.</li>
   * </ul>
   */
  static GenericContainer createAndStartRdbmsProjectionApp(Network klokwrkNetwork, GenericContainer axonServer, PostgreSQLContainer postgresqlServer) {
    String imageVersion = System.getProperty("cargotrackerBookingRdbmsProjectionAppDockerImageVersion")
    Integer[] exposedPorts = [8082]
    String containerName = "cargotracker-booking-queryside-rdbms-projection-app"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer rdbmsProjectionApp = new GenericContainer("klokwrkprj/cargotracker-booking-queryside-rdbms-projection-app:${ imageVersion }")
        .withExposedPorts(exposedPorts)
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv([
            "TZ": "Europe/Zagreb",
            "CARGOTRACKER_AXON_SERVER_HOSTNAME": "${ axonServer.containerInfo.config.hostName }".toString(),
            "CARGOTRACKER_POSTGRES_HOSTNAME": "${ postgresqlServer.containerInfo.name - "/" }".toString(),
            "CARGOTRACKER_POSTGRES_PORT": "5432",
            "CARGOTRACKER_POSTGRES_USERNAME": "cargotracker",
            "CARGOTRACKER_POSTGRES_PASSWORD": "cargotracker",
            // TODO dmurat: also disable Axon Tracing Extension when https://github.com/AxonFramework/extension-tracing/issues/53 will be fixed.
            "MANAGEMENT_METRICS_EXPORT_WAVEFRONT_ENABLED": "false"
        ])
        .withNetwork(klokwrkNetwork)
        .waitingFor(Wait.forHttp("/cargotracker-booking-queryside-rdbms-projection/management/health"))

    rdbmsProjectionApp.start()

    return rdbmsProjectionApp
  }
}
