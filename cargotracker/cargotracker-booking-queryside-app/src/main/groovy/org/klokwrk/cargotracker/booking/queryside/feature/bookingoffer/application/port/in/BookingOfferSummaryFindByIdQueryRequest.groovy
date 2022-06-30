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
package org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2
import org.klokwrk.lib.validation.group.Level3

import javax.validation.GroupSequence
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Null
import javax.validation.constraints.Size

/**
 * Request DTO parameter for {@code bookingOfferSummaryFindByIdQuery} operation from {@link BookingOfferSummaryFindByIdQueryPortIn} inbound port interface.
 * <p/>
 * Here we are comply to the validation ordering as explained in ADR-0013.
 */
@GroupSequence([BookingOfferSummaryFindByIdQueryRequest, Level1, Level2, Level3])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class BookingOfferSummaryFindByIdQueryRequest {
  /**
   * User identifier known to the real end user (i.e., like email)
   * <p/>
   * Not null and not blank.
   * <p/>
   * During request processing, {@code userIdentifier} is converted to the corresponding {@code customerIdentifier} which is used at the database level.
   */
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String userIdentifier

  /**
   * Internal customer identifier.
   * <p/>
   * In inbound request it must be null.
   * <p/>
   * It is assigned during request processing based on corresponding {@code userIdentifier}. {@code customerIdentifier} is used at the database level, while the processing pipeline is responsible to
   * map provided {@code userIdentifier} to {@code customerIdentifier}.
   * <p/>
   * We could introduce another class for complete separation of concerns, i.e., something like {@code BookingOfferSummaryQuery} (and corresponding {@code }BookingOfferSummaryQueryResult}). That
   * {@code BookingOfferSummaryQuery} class would then contain {@code customerIdentifier}, but not {@code userIdentifier} property. Of course, we should also implement appropriate mapping for such a
   * scenario.
   * <p/>
   * However, for simplicity and the smaller number of DTO classes, we have just added the {@code customerIdentifier} property to the already existing {@code BookingOfferSummaryFindByIdQueryRequest}
   * class. In addition, by adding {@code Null} annotation, we ensure the end user does not specify it.
   */
  @Null
  String customerIdentifier

  /**
   * Identifier of a booking offer.
   * <p/>
   * Not null and not blank. Must be in random uuid format.
   * <p/>
   * This particular identifier is used as an aggregate identifier.
   */
  @RandomUuidFormatConstraint(groups = [Level3])
  @Size(min = 36, max = 36, groups = [Level2])
  @NotBlank(groups = [Level1])
  String bookingOfferIdentifier
}
