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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

/**
 * Factory for creating and starting cargotracking-booking-app-commandside in Testcontainers.
 */
@CompileStatic
class CommandSideAppTestcontainersFactory {
  /**
   * Creates and start {@code cargotracking-booking-app-commandside} in container.
   * <p/>
   * <ul>
   *   <li>Container name prefix: {@code cargotracking-booking-app-commandside}.</li>
   *   <li>Exposed internal ports: 8080.</li>
   *   <li>Container time zone: {@code Europe/Zagreb}.</li>
   * </ul>
   */
  static GenericContainer makeAndStartCommandSideApp(Network klokwrkNetwork, GenericContainer axonServer) {
    String imageVersion = System.getProperty("cargotrackerBookingCommandSideAppDockerImageVersion")
    Integer[] exposedPorts = [8080]
    String containerName = "cargotracking-booking-app-commandside"
    String containerNameSuffix = UUID.randomUUID()

    GenericContainer commandSideApp = new GenericContainer("klokwrkprj/cargotracking-booking-app-commandside:${ imageVersion }")

    commandSideApp.with {
      withExposedPorts(exposedPorts)
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv([
          "TZ": "Europe/Zagreb",
          "CARGOTRACKER_AXON_SERVER_HOSTNAME": "${ axonServer.containerInfo.config.hostName }".toString(),
          "MANAGEMENT_DEFAULTS_METRICS_EXPORT_ENABLED": "false",
          "MANAGEMENT_TRACING_ENABLED": "false"
      ])
      withNetwork(klokwrkNetwork)
      waitingFor(Wait.forHttp("/cargotracking-booking-app-commandside/management/health"))

      start()
    }

    return commandSideApp
  }
}
