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
package org.klokwrk.cargotracker.booking.test.support.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

@CompileStatic
class RdbmsManagementAppTestcontainersFactory {
  static GenericContainer makeAndStartRdbmsManagementApp(Network klokwrkNetwork, PostgreSQLContainer postgresqlServer) {
    String imageVersion = System.getProperty("cargotrackerBookingRdbmsManagementAppDockerImageVersion")
    String containerName = "cargotracking-booking-app-rdbms-management"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer rdbmsManagementApp = new GenericContainer("klokwrkprj/cargotracking-booking-app-rdbms-management:${ imageVersion }")

    rdbmsManagementApp.withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
    rdbmsManagementApp.withEnv([
        "TZ": "Europe/Zagreb",
        "CARGOTRACKER_POSTGRES_HOSTNAME": "${ postgresqlServer.containerInfo.name - "/" }".toString(),
        "CARGOTRACKER_POSTGRES_PORT": "5432",
        "CARGOTRACKER_POSTGRES_DB_MIGRATION_USERNAME": "db_migration",
        "CARGOTRACKER_POSTGRES_DB_MIGRATION_PASSWORD": "db_migration",
        "MANAGEMENT_DEFAULTS_METRICS_EXPORT_ENABLED": "false",
        "MANAGEMENT_TRACING_ENABLED": "false"
    ])
    rdbmsManagementApp.withNetwork(klokwrkNetwork)
    rdbmsManagementApp.waitingFor(Wait.forLogMessage(/.*Successfully applied.*migration.*to schema "public".*/, 1))

    rdbmsManagementApp.start()

    return rdbmsManagementApp
  }
}
