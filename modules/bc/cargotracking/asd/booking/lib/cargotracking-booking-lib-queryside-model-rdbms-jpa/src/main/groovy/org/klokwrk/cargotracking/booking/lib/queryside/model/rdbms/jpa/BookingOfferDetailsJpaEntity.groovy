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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkMapConstructorDefaultPostCheck
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkMapConstructorNoArgHideable
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

import static org.hamcrest.Matchers.emptyOrNullString
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

@SuppressWarnings("JpaDataSourceORMInspection")
@ToString
@EqualsAndHashCode(includes = ["bookingOfferId"])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true, useSetters = true)
@KwrkMapConstructorDefaultPostCheck
@KwrkMapConstructorNoArgHideable(makePackagePrivate = true)
@Entity
@Table(name = "booking_offer_details")
@CompileStatic
class BookingOfferDetailsJpaEntity implements PostMapConstructorCheckable {
  @Id
  UUID bookingOfferId

  @Column(nullable = false, updatable = false) String customerId
  @Column(nullable = false) @JdbcTypeCode(SqlTypes.JSON) String details

  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType

  @Column(nullable = false, updatable = false, columnDefinition = "timestamptz") Instant firstEventRecordedAt
  @Column(nullable = false, columnDefinition = "timestamptz") Instant lastEventRecordedAt
  @Column(nullable = false) Long lastEventSequenceNumber

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(bookingOfferId, notNullValue())
    requireMatch(RandomUuidUtils.checkIfRandomUuid(bookingOfferId), is(true))

    requireMatch(details, not(emptyOrNullString()))

    requireMatch(inboundChannelName, not(emptyOrNullString()))
    requireMatch(inboundChannelType, not(emptyOrNullString()))

    requireMatch(firstEventRecordedAt, notNullValue())
    requireMatch(lastEventRecordedAt, notNullValue())
    requireMatch(lastEventSequenceNumber, notNullValue())
    requireMatch(lastEventSequenceNumber, greaterThanOrEqualTo(0L))
  }
}
