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
package org.klokwrk.cargotracking.booking.test.support.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile

@CompileStatic
class PostgreSqlTestcontainersFactory {
  static PostgreSQLContainer makeAndStartPostgreSqlServer(Network klokwrkNetwork) {
    String imageVersion = System.getProperty("postgreSqlDockerImageVersion")
    String containerName = "klokwrk-project-postgres"
    String containerNameSuffix = UUID.randomUUID()

    PostgreSQLContainer postgresqlServer = new PostgreSQLContainer<>("postgres:${ imageVersion }")

    postgresqlServer.with {
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv(["TZ": "Europe/Zagreb"])
      withNetwork(klokwrkNetwork)
      // Here we are explicitly configuring the UTC time zone for the database. For more information, take a look at support/docker/README.md
      withCommand("postgres -c timezone=UTC -c log_timezone=UTC")
      withDatabaseName("postgres")
      withUsername("postgres")
      withPassword("postgres")
      withCopyFileToContainer(MountableFile.forClasspathResource("postgresql/init/1-init_db.sh"), "/docker-entrypoint-initdb.d/1-init_db.sh")

      start()
    }

    return postgresqlServer
  }
}
