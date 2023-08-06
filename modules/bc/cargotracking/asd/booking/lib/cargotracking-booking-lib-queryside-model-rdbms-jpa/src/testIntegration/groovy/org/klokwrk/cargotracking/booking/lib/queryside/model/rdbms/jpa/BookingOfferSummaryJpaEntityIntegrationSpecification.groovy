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
package org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa

import com.github.dockerjava.api.command.CreateNetworkCmd
import org.hibernate.Session
import org.klokwrk.cargotracking.booking.test.support.testcontainers.PostgreSqlTestcontainersFactory
import org.klokwrk.cargotracking.booking.test.support.testcontainers.RdbmsManagementAppTestcontainersFactory
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
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
import java.time.temporal.ChronoUnit

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
  static void configureDynamicTestcontainersProperties(DynamicPropertyRegistry registry) {
    String postgresqlServerHost = postgresqlServer.host
    Integer postgresqlServerPort = postgresqlServer.getMappedPort(5432)

    registry.add("CARGOTRACKING_POSTGRES_HOSTNAME", { "${ postgresqlServerHost }" })
    registry.add("CARGOTRACKING_POSTGRES_PORT", { "${ postgresqlServerPort }" })
  }

  @Autowired
  TestEntityManager testEntityManager

  BookingOfferSummaryJpaEntity originalEntity
  Set<BookingOfferSummaryJpaEntity> entitySet
  Instant currentInstant = Instant.now()

  void setup() {
    originalEntity = new BookingOfferSummaryJpaEntity(
        bookingOfferId: UUID.randomUUID(),

        customerId: UUID.randomUUID().toString(),
        customerType: CustomerType.STANDARD,

        originLocationUnLoCode: "originLocationUnLoCode",
        originLocationName: "originLocationName",
        originLocationCountryName: "originLocationCountryName",

        destinationLocationUnLoCode: "destinationLocationUnLoCode",
        destinationLocationName: "destinationLocationName",
        destinationLocationCountryName: "destinationLocationCountryName",

        departureEarliestTime: currentInstant.plus(1, ChronoUnit.HOURS),
        departureLatestTime: currentInstant.plus(5, ChronoUnit.HOURS),
        arrivalLatestTime: currentInstant.plus(20, ChronoUnit.HOURS),

        commodityTypes: [CommodityType.DRY].toSet(),
        totalCommodityWeight: "1000 kg",
        totalCommodityWeightKg: 1000L,
        totalContainerTeuCount: 2.00G,

        inboundChannelName: "inboundChannelName",
        inboundChannelType: "inboundChannelType",

        firstEventRecordedAt: currentInstant,
        lastEventRecordedAt: currentInstant,
        lastEventSequenceNumber: 0L
    )

    assert originalEntity.propertiesFiltered.size() == 21
    assert originalEntity.commodityTypes == [CommodityType.DRY].toSet()

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
    testEntityManager.entityManager.unwrap(Session).merge(originalEntity)

    then:
    entitySet.contains(originalEntity)
  }

  void "should comply with equality consistency - after reattaching persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    testEntityManager.entityManager.unwrap(Session).merge(originalEntity)

    then:
    entitySet.contains(originalEntity)
  }

  void "should comply with equality consistency - after loading persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity foundEntity = testEntityManager.find(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferId)

    then:
    entitySet.contains(foundEntity)
  }

  void "should comply with equality consistency - after loading a proxy to persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    when:
    BookingOfferSummaryJpaEntity entityProxy = testEntityManager.entityManager.getReference(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferId)

    then:
    entitySet.contains(entityProxy)
    originalEntity == entityProxy
  }

  void "should comply with equality consistency - after loading and removing proxy to persisted entity"() {
    given:
    testEntityManager.persistAndFlush(originalEntity)
    testEntityManager.clear()

    BookingOfferSummaryJpaEntity proxyEntity = testEntityManager.entityManager.getReference(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferId)

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
    BookingOfferSummaryJpaEntity foundEntity = testEntityManager.find(BookingOfferSummaryJpaEntity, originalEntity.bookingOfferId)

    then:
    foundEntity.firstEventRecordedAt.truncatedTo(ChronoUnit.MILLIS) == currentInstant.truncatedTo(ChronoUnit.MILLIS)
    foundEntity.lastEventRecordedAt.truncatedTo(ChronoUnit.MILLIS) == currentInstant.truncatedTo(ChronoUnit.MILLIS)
    foundEntity.lastEventSequenceNumber == 0
  }
}
