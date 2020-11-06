package org.klokwrk.cargotracker.booking.commandside.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import spock.lang.Specification

abstract class AbstractCommandSideIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()
    axonServer = AxonServerTestcontainersFactory.createAndStartAxonServer(klokwrkNetwork)
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.containerIpAddress
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)

    registry.add("axon.axonserver.servers", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })
  }
}
