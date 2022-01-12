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
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2
import org.klokwrk.lib.validation.group.Level3

import javax.validation.GroupSequence
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

/**
 * Request DTO parameter for {@code bookingOfferSummaryQuery} operation from {@link BookingOfferSummaryQueryPortIn} inbound port interface.
 * <p/>
 * Here we are comply to the validation ordering as explained in ADR-0013.
 */
@GroupSequence([BookingOfferSummaryQueryRequest, Level1, Level2, Level3])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class BookingOfferSummaryQueryRequest {
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
