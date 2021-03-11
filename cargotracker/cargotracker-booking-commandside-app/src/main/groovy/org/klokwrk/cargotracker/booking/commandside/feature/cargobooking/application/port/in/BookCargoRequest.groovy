/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import org.klokwrk.lib.validation.constraint.UuidFormatConstraint

import javax.validation.constraints.NotBlank

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class BookCargoRequest {
  // Can be null. If specified, must not be blank and must be in uuid format.
  @NotBlankWhenNullableConstraint
  @UuidFormatConstraint
  String aggregateIdentifier

  // Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation), it also must exist in the location registry (2nd level validation).
  @UnLoCodeFormatConstraint
  @NotBlank
  String originLocation

  // Not null and not blank when not null, and must be in unLoCode format. After formal validation passes (1st level validation) , it also must exist in the location registry (2nd level validation).
  @UnLoCodeFormatConstraint
  @NotBlank
  String destinationLocation
}
