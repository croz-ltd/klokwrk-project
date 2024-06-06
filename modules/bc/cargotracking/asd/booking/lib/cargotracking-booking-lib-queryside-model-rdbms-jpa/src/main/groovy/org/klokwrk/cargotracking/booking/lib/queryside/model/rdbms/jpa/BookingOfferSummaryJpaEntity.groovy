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
package org.klokwrk.cargotracking.booking.lib.queryside.model.rdbms.jpa

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.ToString
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkMapConstructorDefaultPostCheck
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkMapConstructorNoArgHideable
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.blankString
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.emptyOrNullString
import static org.hamcrest.Matchers.everyItem
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.lessThanOrEqualTo
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

// Note about @EqualsAndHashCode:
//   We assume bookingOfferId will never be null when equals and hashCode are used.
//   For this to work, identifier have to be assigned before passing an entity to JPA provider. For user code this is enforced with combination of @KwrkMapConstructorNoArgHideable and
//   @KwrkMapConstructorDefaultPostCheck while Hibernate can still operate with private default constructor.
//
//   @KwrkMapConstructorNoArgHideable is configured to modify no arg constructor's visibility to package-private. This is because Hibernate proxies cannot work with private no-arg constructor.
//   To see the effect, change makePackagePrivate to false, and run BookingOfferSummaryJpaEntityIntegrationSpecification.
//
//   References:
//     https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
//     https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
@SuppressWarnings(["CodeNarc.AbcMetric", "JpaDataSourceORMInspection"])
@ToString
@EqualsAndHashCode(includes = ["bookingOfferId"])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true, useSetters = true)
@KwrkMapConstructorDefaultPostCheck
@KwrkMapConstructorNoArgHideable(makePackagePrivate = true)
@Entity
@Table(name = "booking_offer_summary")
@CompileStatic
class BookingOfferSummaryJpaEntity implements PostMapConstructorCheckable {
  @Id
  UUID bookingOfferId

  @Column(nullable = false, updatable = false) String customerId
  @Column(nullable = false) @Enumerated(EnumType.STRING) CustomerType customerType

  @Column String originLocationUnLoCode
  @Column String originLocationName
  @Column String originLocationCountryName

  @Column String destinationLocationUnLoCode
  @Column String destinationLocationName
  @Column String destinationLocationCountryName

  @Column(columnDefinition = "timestamptz") Instant departureEarliestTime
  @Column(columnDefinition = "timestamptz") Instant departureLatestTime
  @Column(columnDefinition = "timestamptz") Instant arrivalLatestTime

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "booking_offer_summary_commodity_type", joinColumns = @JoinColumn(name = "booking_offer_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "commodity_type")
  Set<CommodityType> commodityTypes

  @Column String totalCommodityWeight

  // totalCommodityWeightKg should be used only for easier and more flexible searching. Should not be used in returned results.
  @Column Long totalCommodityWeightKg

  @Column(precision = 9, scale = 2) BigDecimal totalContainerTeuCount

  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType

  @Column(nullable = false, updatable = false, columnDefinition = "timestamptz") Instant firstEventRecordedAt
  @Column(nullable = false, columnDefinition = "timestamptz") Instant lastEventRecordedAt
  @Column(nullable = false) Long lastEventSequenceNumber

  @SuppressWarnings("CodeNarc.AbcMetric")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(bookingOfferId, notNullValue())
    requireMatch(RandomUuidUtils.checkIfRandomUuid(bookingOfferId), is(true))

    requireMatch(customerId, not(emptyOrNullString()))
    requireMatch(customerType, notNullValue())

    requireMatchWhenNotNull(originLocationUnLoCode, not(blankString()))
    requireMatchWhenNotNull(originLocationName, not(blankString()))
    requireMatchWhenNotNull(originLocationCountryName, not(blankString()))

    requireMatchWhenNotNull(destinationLocationUnLoCode, not(blankString()))
    requireMatchWhenNotNull(destinationLocationName, not(blankString()))
    requireMatchWhenNotNull(destinationLocationCountryName, not(blankString()))

    requireMatchWhenNotNull(commodityTypes, not(empty()))
    requireMatchWhenNotNull(commodityTypes, everyItem(not(blankOrNullString())))

    requireMatchWhenNotNull(totalCommodityWeightKg, greaterThanOrEqualTo(1L))

    requireMatchWhenNotNull(totalContainerTeuCount, greaterThanOrEqualTo(1.0G))
    requireMatchWhenNotNull(totalContainerTeuCount?.scale(), lessThanOrEqualTo(2))

    requireMatch(inboundChannelName, not(blankOrNullString()))
    requireMatch(inboundChannelType, not(blankOrNullString()))

    requireMatch(firstEventRecordedAt, notNullValue())
    requireMatch(lastEventRecordedAt, notNullValue())

    requireMatch(lastEventSequenceNumber, notNullValue())
    requireMatch(lastEventSequenceNumber, greaterThanOrEqualTo(0L))
  }
}
