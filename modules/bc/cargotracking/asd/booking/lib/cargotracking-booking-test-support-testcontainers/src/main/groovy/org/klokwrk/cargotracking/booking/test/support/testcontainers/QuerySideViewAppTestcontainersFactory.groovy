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
package org.klokwrk.cargotracking.booking.test.support.testcontainers

import com.github.dockerjava.api.command.CreateContainerCmd
import groovy.transform.CompileStatic
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting {@code cargotracking-booking-app-queryside-view} in Testcontainers.
 */
@CompileStatic
class QuerySideViewAppTestcontainersFactory {
  /**
   * Creates and start cargotracking-booking-app-queryside-view in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: {@code cargotracking-booking-app-queryside-view}.</li>
   *   <li>Exposed internal ports: 8084.</li>
   *   <li>Container time zone: {@code Europe/Zagreb}.</li>
   * </ul>
   */
  static GenericContainer makeAndStartQuerySideViewApp(Network klokwrkNetwork, GenericContainer axonServer, PostgreSQLContainer postgresqlServer) {
    String imageVersion = System.getProperty("cargotrackingBookingQuerySideViewAppDockerImageVersion")
    Integer[] exposedPorts = [8084]
    String containerName = "cargotracking-booking-app-queryside-view"
    String containerNameSuffix = UUID.randomUUID()

    //noinspection DuplicatedCode
    GenericContainer querySideViewApp = new GenericContainer("klokwrkprj/cargotracking-booking-app-queryside-view:${ imageVersion }")

    querySideViewApp.with {
      withExposedPorts(exposedPorts)
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv([
          "TZ": "Europe/Zagreb",
          "CARGOTRACKING_AXON_SERVER_HOSTNAME": "${ axonServer.containerInfo.config.hostName }".toString(),
          "CARGOTRACKING_POSTGRES_HOSTNAME": "${ postgresqlServer.containerInfo.name - "/" }".toString(),
          "CARGOTRACKING_POSTGRES_PORT": "5432",
          "CARGOTRACKING_POSTGRES_USERNAME": "cargotracking_readonly",
          "CARGOTRACKING_POSTGRES_PASSWORD": "cargotracking_readonly",
          "AXON.EXTENSION.TRACING.ENABLED": "false"
      ])
      withNetwork(klokwrkNetwork)
      waitingFor(Wait.forHttp("/cargotracking-booking-app-queryside-view/management/health"))

      start()
    }

    return querySideViewApp
  }
}
