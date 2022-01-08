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
package org.klokwrk.cargotracker.booking.test.component.replayspec

import com.github.dockerjava.api.command.CreateNetworkCmd
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.klokwrk.cargotracker.booking.commandside.test.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsProjectionAppTestcontainersFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Slf4j
class BookingComponentEventReplaySpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.createAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.createAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)
    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)
    RdbmsProjectionAppTestcontainersFactory.createAndStartRdbmsProjectionApp(klokwrkNetwork, axonServer, postgresqlServer)
  }

  static Integer populateAxonEventStore(GenericContainer axonServerContainer) {
    String replayEventListClasspathLocation = "replayspec/eventList.txt"
    File replayEventListFile = new File(Thread.currentThread().contextClassLoader.getSystemResource(replayEventListClasspathLocation).toURI())

    Integer eventsCount = 0
    List<String> replayEventListToSend = []

    replayEventListFile.eachLine { String replayEventListFileLine ->
      if (replayEventListFileLine.startsWith("data:")) {
        eventsCount++
        String replayEventText = replayEventListFileLine["data:".size()..-1]
        replayEventListToSend << replayEventText

        if (eventsCount % 10 == 0) {
          sendAxonEventMessages(axonServerContainer, replayEventListToSend)
          replayEventListToSend.clear()
        }
      }

      return null
    }

    if (eventsCount % 10 > 0) {
      sendAxonEventMessages(axonServerContainer, replayEventListToSend)
      replayEventListToSend.clear()
    }

    return eventsCount
  }

  private static void sendAxonEventMessages(GenericContainer axonServerContainer, List<String> messageList) {
    //noinspection HttpUrlsUsage
    String axonServerBaseUrl = "http://${ axonServerContainer.containerIpAddress }:${ axonServerContainer.firstMappedPort }"

    String axonServerApiEventsUrl = "$axonServerBaseUrl/v1/events"

    String submitEventsHttpPostBody = /{"messages":[${ messageList.join(",") }]}/
    Request commandRequest = Request.Post(axonServerApiEventsUrl)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Accept-Charset", "utf-8")
                                    .addHeader("AxonIQ-Context", "default")
                                    .bodyString(submitEventsHttpPostBody, ContentType.APPLICATION_JSON)

    HttpResponse commandResponse = commandRequest.execute().returnResponse()
    Integer commandResponseStatusCode = commandResponse.statusLine.statusCode
    if (commandResponseStatusCode >= 400) {
      log.warn("HTTP request resulted in an error. Content body: [$commandResponse.entity.content.text] ")
    }
  }

  static Integer fetchEventGlobalIndexFromRdbmsProjection(PostgreSQLContainer postgresqlServer) {
    Integer storedGlobalIndex = -1

    Sql.withInstance(postgresqlServer.jdbcUrl, postgresqlServer.username, postgresqlServer.password, postgresqlServer.driverClassName) { Sql groovySql ->
      GroovyRowResult queryRowResult = groovySql.firstRow("SELECT token as tokenBlob FROM token_entry WHERE processor_name != '__config'")

      byte[] tokenBlobByteArray = queryRowResult.tokenBlob
      String tokenBlobAsPrintableString = new String(tokenBlobByteArray, "UTF-8")
      Object commandResponseJson = new JsonSlurper().parseText(tokenBlobAsPrintableString)

      //noinspection GrUnresolvedAccess
      storedGlobalIndex = commandResponseJson.globalIndex
    }

    return storedGlobalIndex
  }

  void "should replay rdbms PostgreSQL projection"() {
    given:
    Integer axonServerEventGlobalIndexStart = 0

    when:
    Integer sentEventsCount = populateAxonEventStore(axonServer)

    then:
    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      fetchEventGlobalIndexFromRdbmsProjection(postgresqlServer) == axonServerEventGlobalIndexStart + sentEventsCount - 1
    }
  }
}
