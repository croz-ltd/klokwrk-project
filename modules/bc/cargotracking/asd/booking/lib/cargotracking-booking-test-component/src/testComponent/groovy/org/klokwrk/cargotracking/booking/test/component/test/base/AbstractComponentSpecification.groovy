/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.booking.test.component.test.base

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.klokwrk.cargotracking.booking.test.support.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.CommandSideAppTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.QuerySideProjectionRdbmsAppTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.QuerySideViewAppTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

class AbstractComponentSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork
  static GenericContainer commandSideApp
  static GenericContainer querySideViewApp

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)

    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)

    commandSideApp = CommandSideAppTestcontainersFactory.makeAndStartCommandSideApp(klokwrkNetwork, axonServer)
    QuerySideProjectionRdbmsAppTestcontainersFactory.makeAndStartQuerySideProjectionRdbmsApp(klokwrkNetwork, axonServer, postgresqlServer)
    querySideViewApp = QuerySideViewAppTestcontainersFactory.makeAndStartQuerySideViewApp(klokwrkNetwork, axonServer, postgresqlServer)
  }
}
