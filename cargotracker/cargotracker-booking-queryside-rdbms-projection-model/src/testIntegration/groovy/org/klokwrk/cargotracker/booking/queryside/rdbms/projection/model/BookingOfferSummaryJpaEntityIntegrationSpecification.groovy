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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.hibernate.Session
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracker.booking.queryside.test.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import java.time.Instant

// equality consistency tests reference:
//    https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
//    https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class BookingOfferSummaryJpaEntityIntegrationSpecification extends Specification {
  static PostgreSQLContainer postgresqlServer
  static Network klokwrkNetwork

  static {
    klokwrkNetwork = Network.builder().createNetworkCmdModifier({ CreateNetworkCmd createNetworkCmd -> createNetworkCmd.withName("klokwrk-network-${ UUID.randomUUID() }") }).build()
    postgresqlServer = PostgreSqlTestcontainersFactory.makeAndStartPostgreSqlServer(klokwrkNetwork)
    RdbmsManagementAppTestcontainersFactory.makeAndStartRdbmsManagementApp(klokwrkNetwork, postgresqlServer)
  }

  @DynamicPropertySource
  static void configureAxonServerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", { postgresqlServer.jdbcUrl })
    registry.add("spring.datasource.username", { postgresqlServer.username })
    registry.add("spring.datasource.password", { postgresqlServer.password })
  }

  @Autowired
  TestEntityManager testEntityManager

  BookingOfferSummaryJpaEntity originalEntity
  Set<BookingOfferSummaryJpaEntity> entitySet
  Instant currentInstant = Instant.now()

  void setup() {
    originalEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferIdentifier: UUID.randomUUID(),

        customerIdentifier: UUID.randomUUID(),
        customerType: CustomerType.STANDARD,

        originLocationUnLoCode: "originLocationUnLoCode",
        destinationLocationUnLoCode: "destinationLocationUnLoCode",

        inboundChannelName: "inboundChannelName",
        inboundChannelType: "inboundChannelType",

        firstEventRecordedAt: currentInstant,
        lastEventRecordedAt: currentInstant,
        lastEventSequenceNumber: 0L
    )

    entitySet = [originalEntity] as HashSet
  }

  void "should comply with equality consistency - after persist"() {
    when:
    testEntityManager.persistAndFlush(originalEntity)

    then:
    entitySet.contains(originalEntity)
  }

  void "should comply with equality consistency - after merging non-persisted entity"() {
    when:
    BookingOfferSummaryJpaEntity mergedEntity = testEntityManager.merge(originalEntity)

    then:
    entitySet.contains(mergedEntity)
  }

  void "should comply with equality consistency - after merging persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity mergedEntity = testEntityManager.merge(originalEntity)

    then:
    entitySet.contains(mergedEntity)
  }

  void "should comply with equality consistency - after reattaching non-persisted entity"() {
    when:
    testEntityManager.entityManager.unwrap(Session).update(originalEntity)

    then:
    entitySet.contains(originalEntity)
  }

  void "should comply with equality consistency - after reattaching persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    testEntityManager.entityManager.unwrap(Session).update(originalEntity)

    then:
    entitySet.contains(originalEntity)
  }

  void "should comply with equality consistency - after loading persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity foundEntity = testEntityManager.find(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferIdentifier)

    then:
    entitySet.contains(foundEntity)
  }

  void "should comply with equality consistency - after loading a proxy to persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity entityProxy = testEntityManager.entityManager.getReference(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferIdentifier)

    then:
    entitySet.contains(entityProxy)
    originalEntity == entityProxy
  }

  void "should comply with equality consistency - after loading and removing proxy to persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    BookingOfferSummaryJpaEntity proxyEntity = testEntityManager.entityManager.getReference(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferIdentifier)

    when:
    testEntityManager.remove(proxyEntity)
    testEntityManager.flush()

    then:
    entitySet.contains(proxyEntity)
  }

  void "should correctly store metadata"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity foundEntity = testEntityManager.find(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferIdentifier)

    then:
    foundEntity.firstEventRecordedAt == currentInstant
    foundEntity.lastEventRecordedAt == currentInstant
    foundEntity.lastEventSequenceNumber == 0
  }
}
