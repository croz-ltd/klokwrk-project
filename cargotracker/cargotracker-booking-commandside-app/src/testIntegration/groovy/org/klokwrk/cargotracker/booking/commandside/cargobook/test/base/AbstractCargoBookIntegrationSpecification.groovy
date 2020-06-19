package org.klokwrk.cargotracker.booking.commandside.cargobook.test.base

import com.github.dockerjava.api.command.CreateContainerCmd
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

class AbstractCargoBookIntegrationSpecification extends Specification {
  static GenericContainer axonServer

  static {
    axonServer = createAndStartAxonServer()
  }

  static GenericContainer createAndStartAxonServer() {
    String imageVersion = "4.3.3"
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
        .waitingFor(Wait.forHttp("/v1/public/me").forPort(8024))

    axonServer.start()

    return axonServer
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.containerIpAddress
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)

    registry.add("axon.axonserver.servers", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })
  }
}
