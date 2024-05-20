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
package org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.lo.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.lo.validation.group.Level1
import org.klokwrk.lib.lo.validation.group.Level2
import org.klokwrk.lib.lo.validation.group.Level3
import org.klokwrk.lib.lo.validation.group.Level4
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import jakarta.validation.GroupSequence
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * DTO encapsulating route specification data pieces gathered from inbound ports/adapters.
 */
@GroupSequence([RouteSpecificationRequestData, Level1, Level2, Level3, Level4])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class RouteSpecificationRequestData {
  /**
   * Origin (start) location of a cargo.
   * <p/>
   * Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation), it also must exist in the location registry (2nd level validation).
   */
  @UnLoCodeFormatConstraint(groups = [Level4])
  @TrimmedStringConstraint(groups = [Level3])
  @Size(min = 5, max = 5, groups = [Level2])
  @NotBlank(groups = [Level1])
  String originLocation

  /**
   * Destination (end) location of a cargo.
   * <p/>
   * Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation) , it also must exist in the location registry (2nd level validation).
   */
  @UnLoCodeFormatConstraint(groups = [Level4])
  @TrimmedStringConstraint(groups = [Level3])
  @Size(min = 5, max = 5, groups = [Level2])
  @NotBlank(groups = [Level1])
  String destinationLocation

  /**
   * The earliest time when cargo can be departed.
   * <p/>
   * Not null.
   */
  @NotNull(groups = [Level1])
  Instant departureEarliestTime

  /**
   * The latest time when cargo can be departed.
   * <p/>
   * Not null.
   */
  @NotNull(groups = [Level1])
  Instant departureLatestTime

  /**
   * The latest time when cargo must arrive at the destination.
   * <p/>
   * Not null.
   */
  @NotNull(groups = [Level1])
  Instant arrivalLatestTime
}
