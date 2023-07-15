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
package org.klokwrk.cargotracker.booking.test.component.replayspec

import com.github.dockerjava.api.command.CreateNetworkCmd
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
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

import java.time.Instant

@Slf4j
@Stepwise
class EventReplayComponentSpecification extends Specification {
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
    Request commandRequest = Request.post(axonServerApiEventsUrl)
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .addHeader("Accept-Charset", "utf-8")
        .addHeader("AxonIQ-Context", "default")
        .bodyString(submitEventsHttpPostBody, ContentType.APPLICATION_JSON)

    commandRequest.execute().handleResponse({ ClassicHttpResponse commandHttpResponse ->
      if (commandHttpResponse.code >= 400) {
        log.warn("HTTP request resulted in an error. Content body: [${ commandHttpResponse.entity.content.text }] ")
      }

      return null
    })
  }

  static Tuple2<Integer, Instant> fetchMaxEventGlobalIndexFromProjectionRdbms(PostgreSQLContainer postgresqlServer) {
    Tuple2<Integer, Instant> maxElement = null

    String postgresqlServerJdbcUrl = "jdbc:postgresql://${ postgresqlServer.host }:${ postgresqlServer.getMappedPort(5432) }/cargotracker_booking_query_database"
    String postgresqlServerUsername = "cargotracker_readonly"
    String postgresqlServerPassword = "cargotracker_readonly"
    String postgresqlServerDriverClassName = "org.postgresql.Driver"

    Sql.withInstance(postgresqlServerJdbcUrl, postgresqlServerUsername, postgresqlServerPassword, postgresqlServerDriverClassName) { Sql groovySql ->
      List<GroovyRowResult> queryRowResultList = groovySql.rows("SELECT token as tokenBlob, timestamp FROM token_entry WHERE processor_name != '__config'")

      maxElement = queryRowResultList
          .collect({ GroovyRowResult queryRowResult ->
            Integer globalIndex = null
            byte[] tokenBlobByteArray = queryRowResult?.tokenBlob as byte[]
            if (tokenBlobByteArray != null) {
              String tokenBlobAsPrintableString = new String(tokenBlobByteArray, "UTF-8")
              Object commandResponseJson = new JsonSlurper().parseText(tokenBlobAsPrintableString)
              globalIndex = commandResponseJson.globalIndex
            }

            Instant timestamp = null
            if (queryRowResult?.timestamp) {
              timestamp = Instant.parse(queryRowResult.timestamp as String)
            }

            return new Tuple2<>(globalIndex, timestamp)
          })
          .max { firstElement, secondElement ->
            def (Integer firstGlobalIndex, Instant firstTimestamp) = firstElement
            def (Integer secondGlobalIndex, Instant secondTimestamp) = secondElement
            int globalIndexComparison = firstGlobalIndex <=> secondGlobalIndex
            if (globalIndexComparison != 0) {
              return globalIndexComparison
            }

            int timestampComparison = firstTimestamp <=> secondTimestamp
            return timestampComparison
          }
    }

    return maxElement
  }

  static Tuple2<Integer, Instant> fetchMinFailedEventGlobalIndexFromProjectionRdbms(PostgreSQLContainer postgresqlServer, Instant baseTimestamp) {
    Tuple2<Integer, Instant> minElement = null

    String postgresqlServerJdbcUrl = "jdbc:postgresql://${ postgresqlServer.host }:${ postgresqlServer.getMappedPort(5432) }/cargotracker_booking_query_database"
    String postgresqlServerUsername = "cargotracker_readonly"
    String postgresqlServerPassword = "cargotracker_readonly"
    String postgresqlServerDriverClassName = "org.postgresql.Driver"

    Sql.withInstance(postgresqlServerJdbcUrl, postgresqlServerUsername, postgresqlServerPassword, postgresqlServerDriverClassName) { Sql groovySql ->
      List<GroovyRowResult> queryRowResultList = groovySql.rows(
          "SELECT token as tokenBlob, timestamp FROM token_entry WHERE processor_name != '__config' AND timestamp > ?",
          baseTimestamp.toString()
      )

      minElement = queryRowResultList
          .collect({ GroovyRowResult queryRowResult ->
            Integer globalIndex = null
            byte[] tokenBlobByteArray = queryRowResult?.tokenBlob as byte[]
            if (tokenBlobByteArray != null) {
              String tokenBlobAsPrintableString = new String(tokenBlobByteArray, "UTF-8")
              Object commandResponseJson = new JsonSlurper().parseText(tokenBlobAsPrintableString)
              globalIndex = commandResponseJson.globalIndex
            }

            Instant timestamp = null
            if (queryRowResult?.timestamp) {
              timestamp = Instant.parse(queryRowResult.timestamp as String)
            }

            return new Tuple2<>(globalIndex, timestamp)
          })
          .min { firstElement, secondElement ->
            def (Integer firstGlobalIndex, Instant firstTimestamp) = firstElement
            def (Integer secondGlobalIndex, Instant secondTimestamp) = secondElement
            int globalIndexComparison = firstGlobalIndex <=> secondGlobalIndex
            if (globalIndexComparison != 0) {
              return globalIndexComparison
            }

            int timestampComparison = firstTimestamp <=> secondTimestamp
            return timestampComparison
          }
    }

    return minElement
  }

  void "should correctly project valid events"() {
    given:
    Integer axonServerEventGlobalIndexStart = fetchMaxEventGlobalIndexFromProjectionRdbms(postgresqlServer)?.v1 ?: 0

    when:
    Integer sentEventsCount = populateAxonEventStoreWithValidEvents(axonServer)

    then:
    new PollingConditions(timeout: 5, initialDelay: 0, delay: 0.05).eventually {
      fetchMaxEventGlobalIndexFromProjectionRdbms(postgresqlServer).v1 == axonServerEventGlobalIndexStart + sentEventsCount - 1
    }

    !querySideProjectionRdbmsApp.logs.contains("ERROR")
  }

  void "should not project invalid events"() {
    given:
    def (Integer axonServerEventGlobalIndexStart, Instant axonServerEventGlobalIndexStartInstant) = fetchMaxEventGlobalIndexFromProjectionRdbms(postgresqlServer)

    when:
    populateAxonEventStoreWithInvalidEvents(axonServer)

    then:
    new PollingConditions(timeout: 10, initialDelay: 1, delay: 0.2).eventually {
      fetchMinFailedEventGlobalIndexFromProjectionRdbms(postgresqlServer, axonServerEventGlobalIndexStartInstant).v1 <= axonServerEventGlobalIndexStart
    }

    querySideProjectionRdbmsApp.logs.contains("Error while processing batch in Work Package")
    querySideProjectionRdbmsApp.logs.contains("org.axonframework.serialization.SerializationException: Error while deserializing payload of message")
  }
}
