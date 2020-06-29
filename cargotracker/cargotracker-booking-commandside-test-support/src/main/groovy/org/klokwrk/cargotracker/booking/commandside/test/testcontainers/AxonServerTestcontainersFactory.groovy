package org.klokwrk.cargotracker.booking.commandside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting Axon Server in Testcontainers.
 */
@CompileStatic
class AxonServerTestcontainersFactory {
  /**
   * Creates and start Axon Server in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: <code>klokwrk-project-axon-server</code>.</li>
   *   <li>Exposed internal ports: 8024, 8124.</li>
   *   <li>Container time zone: <code>Europe/Zagreb</code>.</li>
   * </ul>
   */
  static GenericContainer createAndStartAxonServer(Network klokwrkNetwork) {
    String imageVersion = System.getProperty("axonServerDockerImageVersion")
    Integer[] exposedPorts = [8024, 8124]

    String containerName = "klokwrk-project-axon-server"
    // Used for randomization of container name to avoid collisions when multiple tests are run at the same time.
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer axonServer = new GenericContainer("axoniq/axonserver:${ imageVersion }")
        .withExposedPorts(exposedPorts)
        .withCreateContainerCmdModifier({ CreateContainerCmd cmd ->
          cmd.withName("${ containerName }-${ containerNameSuffix }")
        })
        .withEnv(["TZ": "Europe/Zagreb"])
        .withNetwork(klokwrkNetwork)
        .waitingFor(Wait.forHttp("/v1/public/me").forPort(8024))

    axonServer.start()

    return axonServer
  }
}
