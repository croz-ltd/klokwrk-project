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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic

import javax.measure.Quantity
import javax.measure.quantity.Mass

/**
 * Enumerates different characteristic combinations of containers, including dimensions, features, tare weight, and the maximum allowed weight of the contained commodity.
 * <p/>
 * The combination of dimensions and features is expected to be unique across all enum values.
 * <p/>
 * Each {@code ContainerType} enum value is created with four parameters:
 * <ul>
 * <li><b>dimension</b>: length, height, and width of the container.</li>
 * <li><b>featuresType</b>: functional characteristics of a container.</li>
 * <li><b>tareWeight</b>: the weight of an empty container.</li>
 * <li><b>maxCommodityWeight</b>: the maximum weight of carried commodity payload.</li>
 * </ul>
 *
 * Useful references:
 * <ul>
 * <li>https://www.dsv.com/en/our-solutions/modes-of-transport/sea-freight/shipping-container-dimensions/dry-container</li>
 * <li>https://www.dsv.com/en/our-solutions/modes-of-transport/sea-freight/shipping-container-dimensions/reefer-container</li>
 * <li>https://en.wikipedia.org/wiki/ISO_6346</li>
 * <li>https://www.mainfreight.com/romania/en-nz/info-point/ocean-freight-containers</li>
 * <li>https://www.falconstructures.com/blog/container-capacity-chart</li>
 * </ul>
 *
 * @see ContainerDimensionType
 * @see ContainerFeaturesType
 */
@CompileStatic
enum ContainerType {
  /**
   * 10 feet x 8 feet 6 inches x 8 feet general purpose container.
   */
  TYPE_ISO_12G1(ContainerDimensionType.DIMENSION_ISO_12, ContainerFeaturesType.FEATURES_ISO_G1, 1_200.kg, 9_900.kg),

  /**
   * 10 feet x 8 feet 6 inches x 8 feet reefer container.
   */
  TYPE_ISO_12R1_STANDARD_REEFER(ContainerDimensionType.DIMENSION_ISO_12, ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, 1_250.kg, 9_800.kg),

  /**
   * 20 feet x 8 feet 6 inches x 8 feet general purpose container.
   */
  TYPE_ISO_22G1(ContainerDimensionType.DIMENSION_ISO_22, ContainerFeaturesType.FEATURES_ISO_G1, 2_300.kg, 21_700.kg),

  /**
   * 20 feet x 8 feet 6 inches x 8 feet reefer container.
   */
  TYPE_ISO_22R1_STANDARD_REEFER(ContainerDimensionType.DIMENSION_ISO_22, ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, 2_500.kg, 21_600.kg),

  /**
   * 40 feet x 8 feet 6 inches x 8 feet general purpose container.
   */
  TYPE_ISO_42G1(ContainerDimensionType.DIMENSION_ISO_42, ContainerFeaturesType.FEATURES_ISO_G1, 3_700.kg, 26_500.kg),

  /**
   * 40 feet x 8 feet 6 inches x 8 feet reefer container.
   */
  TYPE_ISO_42R1_STANDARD_REEFER(ContainerDimensionType.DIMENSION_ISO_42, ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER, 4_300.kg, 25_000.kg)

  private final ContainerDimensionType dimensionType
  private final ContainerFeaturesType featuresType
  private final Quantity<Mass> tareWeight
  private final Quantity<Mass> maxCommodityWeight

  /**
   * Finds {@code ContainerType} based on provided {@link ContainerDimensionType} and {@link ContainerFeaturesType}.
   * <p/>
   * It is assumed that the combination of {@code containerDimensionType} and {@code containerFeaturesType} is unique across all enum values.
   *
   * @return Returns found {@code ContainerType} or {@code null} otherwise.
   */
  static ContainerType find(ContainerDimensionType containerDimensionType, ContainerFeaturesType containerFeaturesType) {
    ContainerType containerType = values().find({
      ContainerType containerType -> containerType.dimensionType == containerDimensionType && containerType.featuresType == containerFeaturesType
    })

    return containerType
  }

  private ContainerType(ContainerDimensionType dimensionType, ContainerFeaturesType featuresType, Quantity<Mass> tareWeight, Quantity<Mass> maxCommodityWeight) {
    this.dimensionType = dimensionType
    this.featuresType = featuresType
    this.tareWeight = tareWeight
    this.maxCommodityWeight = maxCommodityWeight
  }

  ContainerDimensionType getDimensionType() {
    return dimensionType
  }

  ContainerFeaturesType getFeaturesType() {
    return featuresType
  }

  Quantity<Mass> getTareWeight() {
    return tareWeight
  }

  Quantity<Mass> getMaxCommodityWeight() {
    return maxCommodityWeight
  }
}
