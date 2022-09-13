/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.booking.commandside.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.klokwrk.cargotracker.booking.test.support.testcontainers.AxonServerTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import spock.lang.Specification

abstract class AbstractCommandSideIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static GenericContainer axonServerSecondInstance
  static Network klokwrkNetwork
  static Network klokwrkNetworkSecondInstance

  static {
    klokwrkNetwork = Network
        .builder()
        .createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") })
        .build()

    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)

    klokwrkNetworkSecondInstance = Network
        .builder()
        .createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-secondInstance${ UUID.randomUUID() }") })
        .build()

    axonServerSecondInstance = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetworkSecondInstance, "klokwrk-project-axon-server-secondInstance", 9024, 9124)
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.host
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)

    // Properties axonServerFirstInstanceUrl and axonServerSecondInstanceUrl are used as values for configuring 'axon.axonserver.servers' property at the level of a test class.
    // For concrete example, take a look SpringBootTest annotations of BookingOfferCommandWebControllerIntegrationSpecification,
    // BookingOfferCommandApplicationServiceWithTracingGatewayIntegrationSpecification or BookingOfferCommandApplicationServiceWithDefaultGatewayIntegrationSpecification classes.
    registry.add("axonServerFirstInstanceUrl", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })

    String axonContainerIpAddressSecondInstance = axonServerSecondInstance.host
    Integer axonContainerGrpcPortSecondInstance = axonServerSecondInstance.getMappedPort(9124)
    registry.add("axonServerSecondInstanceUrl", { "${ axonContainerIpAddressSecondInstance }:${ axonContainerGrpcPortSecondInstance }" })
  }
}
