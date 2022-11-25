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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2
import org.klokwrk.lib.validation.group.Level3

import javax.validation.GroupSequence
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * DTO encapsulating commodity info data pieces gathered from external ports/adapters.
 */
@GroupSequence([CommodityInfoData, Level1, Level2, Level3])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class CommodityInfoData {
  /**
   * Commodity type string corresponding to the names of constants from {@link CommodityType} enum.
   * <p/>
   * Must be not {@code null}, not blank, and correspond to one of constant names (ignoring case) from {@link CommodityType} enum.
   */
  @ValueOfEnumConstraint(enumClass = CommodityType, groups = [Level3])
  @TrimmedStringConstraint(groups = [Level2])
  @NotBlank(groups = [Level1])
  String commodityType

  /**
   * Commodity weight in kilograms.
   * <p/>
   * Not {@code null} and must be 1 or greater.
   */
  @Min(value = 1L, groups = [Level2])
  @NotNull(groups = [Level1])
  Integer weightKg

  /**
   * The requested storage temperature in Celsius degree.
   * <p/>
   * Definite storage temperature validation is done by business logic, and it depends on the supported temperature range of the selected commodity type.
   * <p/>
   * Here we are just validating a sensible storage temperature range of [-30, 30] Celsius inclusively to avoid accepting completely unbounded integers. The range chosen here is equal to the
   * supported range of reefer containers.
   */
  @Max(value = 30L, groups = [Level2])
  @Min(value = -30L, groups = [Level2])
  Integer requestedStorageTemperatureInCelsius
}
