package org.klokwrk.cargotracker.booking.commandside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting cargotracker-booking-commandside-app in Testcontainers.
 */
@CompileStatic
class CommandSideAppTestcontainersFactory {
  /**
   * Creates and start cargotracker-booking-commandside-app in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: <code>cargotracker-booking-commandside-app</code>.</li>
   *   <li>Exposed internal ports: 8080.</li>
   *   <li>Container time zone: <code>Europe/Zagreb</code>.</li>
   * </ul>
   */
  static GenericContainer createAndStartCommandSideApp(Network klokwrkNetwork, GenericContainer axonServer) {
    String imageVersion = System.getProperty("cargotrackerBookingCommandSideAppDockerImageVersion")
    Integer[] exposedPorts = [8080]
    String containerName = "cargotracker-booking-commandside-app"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer commandSideApp = new GenericContainer("klokwrkprj/cargotracker-booking-commandside-app:${ imageVersion }")
        .withExposedPorts(exposedPorts)
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv([
            "TZ": "Europe/Zagreb",
            "CARGOTRACKER_AXON_SERVER_HOSTNAME": "${ axonServer.containerInfo.config.hostName }".toString(),
            "MANAGEMENT_METRICS_EXPORT_WAVEFRONT_ENABLED": "false",
            // TODO dmurat: update Axon tracing turn-off when issue is resolved: https://github.com/AxonFramework/extension-tracing/issues/53
            "SPRING_AUTOCONFIGURE_EXCLUDE": "org.axonframework.extensions.tracing.autoconfig.TracingAutoConfiguration"
        ])
        .withNetwork(klokwrkNetwork)
        .waitingFor(Wait.forHttp("/cargotracker-booking-commandside/management/health"))

    commandSideApp.start()

    return commandSideApp
  }
}
