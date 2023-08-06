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
package org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData
import org.klokwrk.lib.lo.validation.constraint.NotBlankWhenNullableConstraint
import org.klokwrk.lib.lo.validation.constraint.NotNullElementsConstraint
import org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint
import org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.lo.validation.group.Level1
import org.klokwrk.lib.lo.validation.group.Level2
import org.klokwrk.lib.lo.validation.group.Level3
import org.klokwrk.lib.lo.validation.group.Level4
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import jakarta.validation.GroupSequence
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO parameter for {@code createBookingOfferCommand} operation from {@link CreateBookingOfferCommandPortIn} inbound port interface.
 * <p/>
 * References:
 * <ul>
 *   <li>https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-default-group-class</li>
 *   <li>https://stackoverflow.com/questions/5571231/control-validation-annotations-order/66264530#66264530</li>
 * </ul>
 */
@GroupSequence([CreateBookingOfferCommandRequest, Level1, Level2, Level3, Level4])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CreateBookingOfferCommandRequest {
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String userId

  /**
   * Optional identifier of a booking offer to be created.
   * <p/>
   * Can be {@code null}. If specified, must not be blank and must be in random uuid format.
   */
  @RandomUuidFormatConstraint(groups = [Level4])
  @TrimmedStringConstraint(groups = [Level3])
  @Size(min = 36, max = 36, groups = [Level2])
  @NotBlankWhenNullableConstraint(groups = [Level1])
  String bookingOfferId

  /**
   * Route specification data.
   * <p/>
   * Must be not {@code null} and valid.
   */
  @Valid
  @NotNull(groups = [Level1])
  RouteSpecificationRequestData routeSpecification

  /**
   * Cargo data.
   * <p/>
   * Must be not {@code null} and valid.
   */
  @Valid
  @NotNullElementsConstraint(groups = [Level2])
  @NotEmpty(groups = [Level1])
  Collection<CargoRequestData> cargos
}
