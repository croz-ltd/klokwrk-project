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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.quantity.QuantityRange
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Temperature

import static org.hamcrest.Matchers.notNullValue

/**
 * Enumerates types of commodities.
 * <p/>
 * Available commodity types are:
 * <ul>
 *   <li><b>DRY</b>: does not require controlled temperature.</li>
 *   <li><b>AIR_COOLED</b>: requires an air-cooled temperature environment. The allowed temperature range is [2, 12] Celsius inclusive.</li>
 *   <li><b>CHILLED</b>: requires a chilled temperature environment. The allowed temperature range is [-2, 6] Celsius inclusive.</li>
 *   <li><b>FROZEN</b>: requires a frozen temperature environment. The allowed temperature range is [-20, -8] Celsius inclusive.</li>
 * </ul>
 *
 * Each {@code CommodityType} enum value is created with three parameters:
 * <ul>
 * <li><b>containerFeaturesType</b>: specifies container features required for this commodity type (i.e., reefer container or general-purpose container).</li>
 * <li>
 *   <b>recommendedStorageTemperature</b>: specifies recommended temperature at which we should keep this commodity during transport. When a customer does not provide the desired storage
 *   temperature, it is used as a default value.
 * </li>
 * <li><b>storageTemperatureRange</b>: specifies an inclusive temperature range appropriate for this commodity type.</li>
 * </ul>
 *
 * @see ContainerFeaturesType
 */
@CompileStatic
enum CommodityType {
  DRY(ContainerFeaturesType.FEATURES_ISO_G1, null, Constants.UNLIMITED_TEMPERATURE_RANGE),
  AIR_COOLED(ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, Constants.AIR_COOLED_RECOMMENDED_STORAGE_TEMPERATURE, Constants.AIR_COOLED_STORAGE_TEMPERATURE_RANGE),
  CHILLED(ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, Constants.CHILLED_RECOMMENDED_STORAGE_TEMPERATURE, Constants.CHILLED_STORAGE_TEMPERATURE_RANGE),
  FROZEN(ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, Constants.FROZEN_RECOMMENDED_STORAGE_TEMPERATURE, Constants.FROZEN_STORAGE_TEMPERATURE_RANGE)

  @SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
  static class Constants {
    static final QuantityRange<Temperature> UNLIMITED_TEMPERATURE_RANGE = QuantityRange.of(null, null)

    static final Quantity<Temperature> AIR_COOLED_RECOMMENDED_STORAGE_TEMPERATURE = Quantities.getQuantity(6, Units.CELSIUS)
    static final QuantityRange<Temperature> AIR_COOLED_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(2, Units.CELSIUS), Quantities.getQuantity(12, Units.CELSIUS))

    static final Quantity<Temperature> CHILLED_RECOMMENDED_STORAGE_TEMPERATURE = Quantities.getQuantity(0, Units.CELSIUS)
    static final QuantityRange<Temperature> CHILLED_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(-2, Units.CELSIUS), Quantities.getQuantity(6, Units.CELSIUS))

    static final Quantity<Temperature> FROZEN_RECOMMENDED_STORAGE_TEMPERATURE = Quantities.getQuantity(-12, Units.CELSIUS)
    static final QuantityRange<Temperature> FROZEN_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(-20, Units.CELSIUS), Quantities.getQuantity(-8, Units.CELSIUS))
  }

  private final ContainerFeaturesType containerFeaturesType
  private final Quantity<Temperature> recommendedStorageTemperature
  private final QuantityRange<Temperature> storageTemperatureRange

  private CommodityType(ContainerFeaturesType containerFeaturesType, Quantity<Temperature> recommendedStorageTemperature, QuantityRange<Temperature> storageTemperatureRange) {
    this.containerFeaturesType = containerFeaturesType
    this.recommendedStorageTemperature = recommendedStorageTemperature
    this.storageTemperatureRange = storageTemperatureRange
  }

  ContainerFeaturesType getContainerFeaturesType() {
    return containerFeaturesType
  }

  Quantity<Temperature> getRecommendedStorageTemperature() {
    return recommendedStorageTemperature
  }

  QuantityRange<Temperature> getStorageTemperatureRange() {
    return storageTemperatureRange
  }

  /**
   * Answers if the storageTemperatureRange of this commodity type is limited (i.e., requires reefer container) or not.
   */
  Boolean isStorageTemperatureLimited() {
    return storageTemperatureRange != Constants.UNLIMITED_TEMPERATURE_RANGE
  }

  /**
   * Determines if the provided temperature is allowed by {@code storageTemperatureRange} of this commodity type.
   * <p/>
   * For the {@code null} value of the {@code temperature} parameter, throws {@code AssertionError}.<br/>
   * Otherwise, when {@code recommendedStorageTemperature} of this commodity type is {@code null}, it returns {@code true}.<br/>
   * Otherwise, it checks if the provided temperature is contained in the storage temperature range of this commodity type.
   */
  Boolean isStorageTemperatureAllowed(Quantity<Temperature> temperature) {
    requireMatch(temperature, notNullValue())

    if (recommendedStorageTemperature == null) {
      return true
    }

    return getStorageTemperatureRange().contains(temperature)
  }
}
