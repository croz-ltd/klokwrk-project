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
package org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.ToString
import io.hypersistence.utils.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.misc.RandomUuidUtils
import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck
import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import java.time.Instant

import static org.hamcrest.Matchers.emptyOrNullString
import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

@ToString
@EqualsAndHashCode(includes = ["bookingOfferIdentifier"])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true, useSetters = true)
@KwrkMapConstructorDefaultPostCheck
@KwrkMapConstructorNoArgHideable(makePackagePrivate = true)
@TypeDef(name = "json", typeClass = JsonType)
@Entity
@Table(name = "booking_offer_details")
@CompileStatic
class BookingOfferDetailsJpaEntity implements PostMapConstructorCheckable {
  @Id
  UUID bookingOfferIdentifier

  @Column(nullable = false, updatable = false) String customerIdentifier
  @Column(nullable = false) @Type(type = "json") String details

  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType

  @Column(nullable = false, updatable = false, columnDefinition = "timestamptz") Instant firstEventRecordedAt
  @Column(nullable = false, columnDefinition = "timestamptz") Instant lastEventRecordedAt
  @Column(nullable = false) Long lastEventSequenceNumber

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(bookingOfferIdentifier, notNullValue())
    requireMatch(RandomUuidUtils.checkIfRandomUuid(bookingOfferIdentifier), is(true))

    requireMatch(details, not(emptyOrNullString()))

    requireMatch(inboundChannelName, not(emptyOrNullString()))
    requireMatch(inboundChannelType, not(emptyOrNullString()))

    requireMatch(firstEventRecordedAt, notNullValue())
    requireMatch(lastEventRecordedAt, notNullValue())
    requireMatch(lastEventSequenceNumber, notNullValue())
    requireMatch(lastEventSequenceNumber, greaterThanOrEqualTo(0L))
  }
}
