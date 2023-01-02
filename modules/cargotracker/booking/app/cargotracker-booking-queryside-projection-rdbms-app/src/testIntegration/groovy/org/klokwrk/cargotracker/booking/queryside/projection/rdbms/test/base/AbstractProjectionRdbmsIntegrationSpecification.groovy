/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.projection.rdbms.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.klokwrk.cargotracker.booking.test.support.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.test.support.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.test.support.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

abstract class AbstractProjectionRdbmsIntegrationSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)
  }

  @DynamicPropertySource
  static void configureDynamicTestcontainersProperties(DynamicPropertyRegistry registry) {
    String axonContainerHost = axonServer.host
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)
    registry.add("CARGOTRACKER_AXON_SERVER_HOSTNAME", { axonContainerHost })
    registry.add("CARGOTRACKER_AXON_SERVER_PORT_GRPC", { "${ axonContainerGrpcPort }" })

    String postgresqlServerHost = postgresqlServer.host
    Integer postgresqlServerPort = postgresqlServer.getMappedPort(5432)
    registry.add("CARGOTRACKER_POSTGRES_HOSTNAME", { "${ postgresqlServerHost }" })
    registry.add("CARGOTRACKER_POSTGRES_PORT", { "${ postgresqlServerPort }" })
  }
}
