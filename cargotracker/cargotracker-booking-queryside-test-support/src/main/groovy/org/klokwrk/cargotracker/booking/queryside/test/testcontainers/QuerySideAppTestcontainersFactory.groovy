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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting cargotracker-booking-queryside-app in Testcontainers.
 */
@CompileStatic
class QuerySideAppTestcontainersFactory {
  /**
   * Creates and start cargotracker-booking-queryside-app in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: <code>cargotracker-booking-queryside-app</code>.</li>
   *   <li>Exposed internal ports: 8084.</li>
   *   <li>Container time zone: <code>Europe/Zagreb</code>.</li>
   * </ul>
   */
  static GenericContainer createAndStartQuerySideApp(Network klokwrkNetwork, GenericContainer axonServer, PostgreSQLContainer postgresqlServer) {
    String imageVersion = System.getProperty("cargotrackerBookingQuerySideAppDockerImageVersion")
    Integer[] exposedPorts = [8084]
    String containerName = "cargotracker-booking-queryside-app"
    String containerNameSuffix = UUID.randomUUID()

    //noinspection DuplicatedCode
    GenericContainer querySideApp = new GenericContainer("klokwrkprj/cargotracker-booking-queryside-app:${ imageVersion }")

    querySideApp.with {
      withExposedPorts(exposedPorts)
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv([
          "TZ": "Europe/Zagreb",
          "CARGOTRACKER_AXON_SERVER_HOSTNAME": "${ axonServer.containerInfo.config.hostName }".toString(),
          "CARGOTRACKER_POSTGRES_HOSTNAME": "${ postgresqlServer.containerInfo.name - "/" }".toString(),
          "CARGOTRACKER_POSTGRES_PORT": "5432",
          "CARGOTRACKER_POSTGRES_USERNAME": "cargotracker",
          "CARGOTRACKER_POSTGRES_PASSWORD": "cargotracker",
          "MANAGEMENT_METRICS_EXPORT_WAVEFRONT_ENABLED": "false",
          "AXON.EXTENSION.TRACING.ENABLED": "false"
      ])
      withNetwork(klokwrkNetwork)
      waitingFor(Wait.forHttp("/cargotracker-booking-queryside/management/health"))

      start()
    }

    return querySideApp
  }
}
