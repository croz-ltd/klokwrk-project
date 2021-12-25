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
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Describes an individual container in terms of container type and contained commodity info.
 *
 * @see ContainerType
 * @see ContainerCommodityInfo
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class ContainerInfo implements PostMapConstructorCheckable {
  ContainerType containerType
  ContainerCommodityInfo containerCommodityInfo

  /**
   * The simple factory method that just delegates to the map constructor.
   */
  static ContainerInfo create(ContainerType containerType, ContainerCommodityInfo containerCommodityInfo) {
    return new ContainerInfo(containerType: containerType, containerCommodityInfo: containerCommodityInfo)
  }

  /**
   * The factory method that creates {@code ContainerInfo} instance based on {@link ContainerDimensionType} and {@link ContainerCommodityInfo}.
   * <p/>
   * It is assumed that the combination of {@code containerDimensionType} and {@code containerCommodityInfo.commodityType.containerFeaturesType} is unique across all {@link ContainerType} enum values.
   */
  static ContainerInfo create(ContainerDimensionType containerDimensionType, ContainerCommodityInfo containerCommodityInfo) {
    ContainerType containerType = ContainerType.find(containerDimensionType, containerCommodityInfo.commodityType.containerFeaturesType)
    ContainerInfo containerInfo = create(containerType, containerCommodityInfo)
    return containerInfo
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(containerType, notNullValue())
    requireMatch(containerCommodityInfo, notNullValue())

    requireTrue(containerType.featuresType == containerCommodityInfo.commodityType.containerFeaturesType)

    ComparableQuantity<Mass> inContainerCommodityWeight = Quantities.getQuantity(containerCommodityInfo.commodityInContainerWeight.value, containerCommodityInfo.commodityInContainerWeight.unit)
    requireTrue(inContainerCommodityWeight.isLessThanOrEqualTo(containerType.maxCommodityWeight))
  }

  /**
   * Calculates the verified gross mass (VGM) of the container.
   * <p/>
   * The verified gross mass is the sum of empty container weight (tare weight) and in container stored commodity weight.
   */
  Quantity<Mass> calculateVerifiedGrossMass() {
    return containerType.tareWeight.add(containerCommodityInfo.commodityInContainerWeight)
  }
}
