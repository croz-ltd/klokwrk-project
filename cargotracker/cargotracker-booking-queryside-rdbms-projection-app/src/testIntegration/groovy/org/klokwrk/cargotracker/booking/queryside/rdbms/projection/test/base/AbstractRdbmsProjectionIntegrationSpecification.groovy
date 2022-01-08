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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

abstract class AbstractRdbmsProjectionIntegrationSpecification extends Specification {
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
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    String axonContainerIpAddress = axonServer.containerIpAddress
    Integer axonContainerGrpcPort = axonServer.getMappedPort(8124)
    registry.add("axon.axonserver.servers", { "${ axonContainerIpAddress }:${ axonContainerGrpcPort }" })

    registry.add("spring.datasource.url", { postgresqlServer.jdbcUrl })
    registry.add("spring.datasource.username", { postgresqlServer.username })
    registry.add("spring.datasource.password", { postgresqlServer.password })
  }
}
