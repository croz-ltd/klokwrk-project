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
import org.klokwrk.cargotracker.booking.test.support.testcontainers.AxonServerTestcontainersFactory
import org.klokwrk.cargotracker.booking.test.support.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.test.support.testcontainers.QuerySideProjectionRdbmsAppTestcontainersFactory
import org.klokwrk.cargotracker.booking.test.support.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

@Slf4j
@Stepwise
class BookingEventReplayComponentSpecification extends Specification {
  static GenericContainer axonServer
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork
  static GenericContainer querySideProjectionRdbmsApp

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()

    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)
    axonServer = AxonServerTestcontainersFactory.makeAndStartAxonServer(klokwrkNetwork)
    querySideProjectionRdbmsApp = QuerySideProjectionRdbmsAppTestcontainersFactory.makeAndStartQuerySideProjectionRdbmsApp(klokwrkNetwork, axonServer, postgresqlServer)
  }

  static Integer populateAxonEventStoreWithValidEvents(GenericContainer axonServerContainer) {
    String replayEventListClasspathLocation = "replayspec/validEventList.txt"
    Integer populatedEventsCount = populateAxonEventStore(axonServerContainer, replayEventListClasspathLocation)
    return populatedEventsCount
  }

  static Integer populateAxonEventStoreWithInvalidEvents(GenericContainer axonServerContainer) {
    String replayEventListClasspathLocation = "replayspec/invalidEventList.txt"
    Integer populatedEventsCount = populateAxonEventStore(axonServerContainer, replayEventListClasspathLocation)
    return populatedEventsCount
  }

  static Integer populateAxonEventStore(GenericContainer axonServerContainer, String replayEventListClasspathLocation) {
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
    Integer axonServerHttpPort = 8024
    //noinspection HttpUrlsUsage
    String axonServerBaseUrl = "http://${ axonServerContainer.host }:${ axonServerContainer.getMappedPort(axonServerHttpPort) }"

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

  static Integer fetchEventGlobalIndexFromProjectionRdbms(PostgreSQLContainer postgresqlServer) {
    Integer storedGlobalIndex = -1

    String postgresqlServerJdbcUrl = "jdbc:postgresql://${ postgresqlServer.host }:${ postgresqlServer.getMappedPort(5432) }/cargotracker_booking_query_database"
    String postgresqlServerUsername = "cargotracker_readonly"
    String postgresqlServerPassword = "cargotracker_readonly"
    String postgresqlServerDriverClassName = "org.postgresql.Driver"

    Sql.withInstance(postgresqlServerJdbcUrl, postgresqlServerUsername, postgresqlServerPassword, postgresqlServerDriverClassName) { Sql groovySql ->
      GroovyRowResult queryRowResult = groovySql.firstRow("SELECT token as tokenBlob FROM token_entry WHERE processor_name != '__config'")

      byte[] tokenBlobByteArray = queryRowResult.tokenBlob as byte[]
      String tokenBlobAsPrintableString = new String(tokenBlobByteArray, "UTF-8")
      Object commandResponseJson = new JsonSlurper().parseText(tokenBlobAsPrintableString)

      //noinspection GrUnresolvedAccess
      storedGlobalIndex = commandResponseJson.globalIndex
    }

    return storedGlobalIndex
  }

  void "should correctly project valid events"() {
    given:
    Integer axonServerEventGlobalIndexStart = 0

    when:
    Integer sentEventsCount = populateAxonEventStoreWithValidEvents(axonServer)

    then:
    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      fetchEventGlobalIndexFromProjectionRdbms(postgresqlServer) == axonServerEventGlobalIndexStart + sentEventsCount - 1
    }

    !querySideProjectionRdbmsApp.logs.contains("ERROR")
  }

  void "should not project invalid events"() {
    given:
    Integer axonServerEventGlobalIndexStart = fetchEventGlobalIndexFromProjectionRdbms(postgresqlServer)

    when:
    populateAxonEventStoreWithInvalidEvents(axonServer)

    then:
    new PollingConditions(timeout: 5, initialDelay: 3, delay: 0.05).eventually {
      fetchEventGlobalIndexFromProjectionRdbms(postgresqlServer) == axonServerEventGlobalIndexStart
    }

    querySideProjectionRdbmsApp.logs.contains("org.axonframework.serialization.SerializationException: Error while deserializing payload of message")
    querySideProjectionRdbmsApp.logs.contains("Releasing claim on token and preparing for retry")
  }
}
