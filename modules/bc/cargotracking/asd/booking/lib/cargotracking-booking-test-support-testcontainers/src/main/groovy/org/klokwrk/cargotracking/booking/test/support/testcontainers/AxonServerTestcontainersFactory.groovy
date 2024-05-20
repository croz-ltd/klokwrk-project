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
import org.testcontainers.containers.wait.strategy.Wait

import java.time.Duration

/**
 * Factory for creating and starting Axon Server in Testcontainers.
 */
@CompileStatic
class AxonServerTestcontainersFactory {
  /**
   * Creates and start Axon Server in container.
   * <p/>
   * <ul>
   *   <li>Default container name prefix: {@code klokwrk-project-axon-server}.</li>
   *   <li>Default exposed internal ports are 8024 for http port and 8124 for GRPC port.</li>
   *   <li>Container time zone is set to {@code Europe/Zagreb}.</li>
   * </ul>
   */
  static GenericContainer makeAndStartAxonServer(
      Network klokwrkNetwork, String containerName = "klokwrk-project-axon-server", String hostName = "klokwrk-project-axon-server", Integer httpPort = 8024, Integer grpcPort = 8124)
  {
    String imageVersion = System.getProperty("axonServerDockerImageVersion")
    Integer[] exposedPorts = [httpPort, grpcPort]

    // Used for randomization of container name to avoid collisions when multiple tests are run at the same time.
    String containerNameSuffix = UUID.randomUUID()
    GenericContainer axonServer = new GenericContainer("axoniq/axonserver:${ imageVersion }")

    axonServer.with {
      withExposedPorts(exposedPorts)
      withCreateContainerCmdModifier({ CreateContainerCmd cmd -> cmd.withName("${ containerName }-${ containerNameSuffix }") })
      withEnv([
          "TZ": "Europe/Zagreb",
          "AXONIQ_AXONSERVER_HOSTNAME": "$hostName".toString(),
          "AXONIQ_AXONSERVER_AUTOCLUSTER_FIRST": "$hostName".toString(),
          "AXONIQ_AXONSERVER_AUTOCLUSTER_CONTEXTS": "default"
      ])
      withNetwork(klokwrkNetwork)
      waitingFor(Wait.forLogMessage(/.*default: context default created.*/, 1).withStartupTimeout(Duration.ofMinutes(10)))

      start()
    }

    return axonServer
  }
}
