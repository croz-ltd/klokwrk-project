/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.test.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer

@CompileStatic
class PostgreSqlTestcontainersFactory {
  static PostgreSQLContainer createAndStartPostgreSqlServer(Network klokwrkNetwork) {
    String imageVersion = System.getProperty("postgreSqlDockerImageVersion")
    String containerName = "klokwrk-project-postgres"
    String containerNameSuffix = UUID.randomUUID()

    PostgreSQLContainer postgresqlServer = new PostgreSQLContainer<>("postgres:${ imageVersion }")

    postgresqlServer.with {
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv(["TZ": "Europe/Zagreb"])
      withNetwork(klokwrkNetwork)
      withDatabaseName("cargotracker_booking_query_database")
      withUsername("cargotracker")
      withPassword("cargotracker")

      start()
    }

    return postgresqlServer
  }
}
