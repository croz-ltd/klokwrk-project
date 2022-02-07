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

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.ToString
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.misc.RandomUuidUtils
import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck
import org.klokwrk.lang.groovy.transform.KwrkMapConstructorNoArgHideable
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue

// Note about @EqualsAndHashCode:
//   We assume bookingOfferIdentifier will never be null when equals and hashCode are used.
//   For this to work, identifier have to be assigned before passing an entity to JPA provider. For user code this is enforced with combination of @KwrkMapConstructorNoArgHideable and
//   @KwrkMapConstructorDefaultPostCheck while Hibernate can still operate with private default constructor.
//
//   @KwrkMapConstructorNoArgHideable is configured to modify no arg constructor's visibility to package-private. This is because Hibernate proxies cannot work with private no-arg constructor.
//   To see the effect, change makePackagePrivate to false, and run BookingOfferSummaryJpaEntityIntegrationSpecification.
//
//   References:
//     https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
//     https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
@ToString
@EqualsAndHashCode(includes = ["bookingOfferIdentifier"])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@KwrkMapConstructorDefaultPostCheck
@KwrkMapConstructorNoArgHideable(makePackagePrivate = true)
@Entity
@CompileStatic
@Table(name = "booking_offer_summary")
class BookingOfferSummaryJpaEntity implements PostMapConstructorCheckable {
  @Id
  UUID bookingOfferIdentifier

  @Column(nullable = false) String originLocation
  @Column(nullable = false) String destinationLocation

  @Column(nullable = false) Long aggregateVersion
  @Column(nullable = false) String inboundChannelName
  @Column(nullable = false) String inboundChannelType

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(bookingOfferIdentifier, notNullValue())
    requireMatch(RandomUuidUtils.checkIfRandomUuidString(bookingOfferIdentifier.toString()), is(true))
  }
}
