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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

import static org.hamcrest.Matchers.notNullValue

/**
 * Describes commodity characteristics at the level of a single container.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class ContainerCommodityInfo implements PostMapConstructorCheckable {
  /**
   * Type of commodity.
   * <p/>
   * Must not be {@code null}.
   */
  CommodityType commodityType

  /**
   * The weight of commodity stored in an individual container.
   * <p/>
   * Must not be {@code null}, and must be at least 1 kg or greater. Do note it does not have to be specified in kilograms.
   */
  Quantity<Mass> commodityInContainerWeight

  /**
   * Requested storage temperature of a commodity for an individual container.
   * <p/>
   * Whether it is required or not depends on the commodity type. If required, it must be inside of temperature range boundaries of the corresponding commodity type.
   */
  Quantity<Temperature> commodityRequestedStorageTemperature

  static ContainerCommodityInfo create(CommodityType commodityType, Quantity<Mass> commodityInContainerWeight) {
    ContainerCommodityInfo containerCommodityInfo = create(commodityType, commodityInContainerWeight, null)
    return containerCommodityInfo
  }

  /**
   * The main factory method for creating {@code ContainerCommodityInfo} instance (all other {@code create()} factory methods delegate to this one).
   * <p/>
   * The only optional parameter (can be provided as {@code null}) is {@code commodityRequestedStorageTemperature}. When {@code null}, the actual {code commodityRequestedStorageTemperature} of the
   * instance is set to the {@code recommendedStorageTemperature} of provided {@code commodityType}. Note that the {@code recommendedStorageTemperature} of {@code commodityType} can be {@code null}.
   */
  static ContainerCommodityInfo create(CommodityType commodityType, Quantity<Mass> commodityInContainerWeight, Quantity<Temperature> commodityRequestedStorageTemperature) {
    Quantity<Temperature> commodityRequestedStorageTemperatureToUse = commodityRequestedStorageTemperature
    if (commodityRequestedStorageTemperature == null && commodityType != null) {
      commodityRequestedStorageTemperatureToUse = commodityType.recommendedStorageTemperature
    }

    ContainerCommodityInfo containerCommodityInfo = new ContainerCommodityInfo(
        commodityType: commodityType, commodityInContainerWeight: commodityInContainerWeight, commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureToUse
    )

    return containerCommodityInfo
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(commodityType, notNullValue())
    requireMatch(commodityInContainerWeight, notNullValue())

    requireTrue(Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(commodityInContainerWeight))

    requireTrue(isRequestedStorageTemperatureAvailableWhenNeeded(commodityRequestedStorageTemperature, commodityType))
    requireTrue(isRequestedStorageTemperatureInAllowedRange(commodityRequestedStorageTemperature, commodityType))
  }

  private Boolean isRequestedStorageTemperatureAvailableWhenNeeded(Quantity<Temperature> commodityRequestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.containerFeaturesType.isContainerTemperatureControlled() && commodityRequestedStorageTemperature == null) {
      return false
    }

    return true
  }

  private Boolean isRequestedStorageTemperatureInAllowedRange(Quantity<Temperature> commodityRequestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.isStorageTemperatureLimited() && (!commodityType.isStorageTemperatureAllowed(commodityRequestedStorageTemperature))) {
      return false
    }

    return true
  }
}
