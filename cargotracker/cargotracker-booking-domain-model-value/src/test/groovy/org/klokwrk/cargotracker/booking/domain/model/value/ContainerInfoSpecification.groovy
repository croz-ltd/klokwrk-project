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

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class ContainerInfoSpecification extends Specification {
  static ContainerCommodityInfo validDryContainerCommodityInfo = ContainerCommodityInfo.create(CommodityType.DRY, Quantities.getQuantity(1000, Units.KILOGRAM))
  static ContainerCommodityInfo validAirCooledContainerCommodityInfo = ContainerCommodityInfo.create(CommodityType.AIR_COOLED, Quantities.getQuantity(1000, Units.KILOGRAM))

  void "map constructor should work for correct input params"() {
    given:
    ContainerType containerType = containerTypeParam
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, Quantities.getQuantity(1000, Units.KILOGRAM), commodityRequestedStorageTemperatureParam)

    when:
    ContainerInfo containerInfo = new ContainerInfo(containerType: containerType, containerCommodityInfo: containerCommodityInfo)

    then:
    containerInfo

    where:
    containerTypeParam                          | commodityTypeParam       | commodityRequestedStorageTemperatureParam
    ContainerType.TYPE_ISO_22G1                 | CommodityType.DRY        | null
    ContainerType.TYPE_ISO_22G1                 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)

    ContainerType.TYPE_ISO_42G1                 | CommodityType.DRY        | null
    ContainerType.TYPE_ISO_42G1                 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)

    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)

    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)
  }

  void "map constructor should fail for null input params"() {
    when:
    new ContainerInfo(containerType: containerTypeParam, containerCommodityInfo: containerCommodityInfoParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    containerTypeParam          | containerCommodityInfoParam
    null                        | validDryContainerCommodityInfo
    ContainerType.TYPE_ISO_22G1 | null
  }

  void "map constructor should fail when container featuresType does not match"() {
    when:
    new ContainerInfo(containerType: containerTypeParam, containerCommodityInfo: containerCommodityInfoParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: (containerType.featuresType == containerCommodityInfo.commodityType.containerFeaturesType)]")

    where:
    containerTypeParam                          | containerCommodityInfoParam
    ContainerType.TYPE_ISO_22G1                 | validAirCooledContainerCommodityInfo
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | validDryContainerCommodityInfo
  }

  void "map constructor should fail when inContainerCommodityWeight is too large"() {
    given:
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(CommodityType.DRY, Quantities.getQuantity(50000, Units.KILOGRAM))

    when:
    new ContainerInfo(containerType: ContainerType.TYPE_ISO_22G1, containerCommodityInfo: containerCommodityInfo)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: inContainerCommodityWeight.isLessThanOrEqualTo(containerType.maxCommodityWeight)]")
  }

  void "create(ContainerType, ContainerCommodityInfo) method should work for correct input params"() {
    given:
    ContainerType containerType = containerTypeParam
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, Quantities.getQuantity(1000, Units.KILOGRAM), commodityRequestedStorageTemperatureParam)

    when:
    ContainerInfo containerInfo = ContainerInfo.create(containerType, containerCommodityInfo)

    then:
    containerInfo

    where:
    containerTypeParam                          | commodityTypeParam       | commodityRequestedStorageTemperatureParam
    ContainerType.TYPE_ISO_22G1                 | CommodityType.DRY        | null
    ContainerType.TYPE_ISO_22G1                 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)

    ContainerType.TYPE_ISO_42G1                 | CommodityType.DRY        | null
    ContainerType.TYPE_ISO_42G1                 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)

    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.AIR_COOLED | null
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.CHILLED    | null
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.FROZEN     | null
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)

    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.AIR_COOLED | null
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.CHILLED    | null
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.FROZEN     | null
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)
  }

  void "create(ContainerDimensionType, ContainerCommodityInfo) method should work for correct input params"() {
    given:
    ContainerDimensionType containerDimensionType = containerDimensionTypeParam
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, Quantities.getQuantity(1000, Units.KILOGRAM), commodityRequestedStorageTemperatureParam)

    when:
    ContainerInfo containerInfo = ContainerInfo.create(containerDimensionType, containerCommodityInfo)

    then:
    containerInfo

    where:
    containerDimensionTypeParam             | commodityTypeParam       | commodityRequestedStorageTemperatureParam
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.DRY        | null
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.AIR_COOLED | null
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.CHILLED    | null
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.FROZEN     | null
    ContainerDimensionType.DIMENSION_ISO_22 | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)

    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.DRY        | null
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.DRY        | Quantities.getQuantity(25, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.AIR_COOLED | null
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.AIR_COOLED | Quantities.getQuantity(10, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.CHILLED    | null
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.CHILLED    | Quantities.getQuantity(3, Units.CELSIUS)
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.FROZEN     | null
    ContainerDimensionType.DIMENSION_ISO_42 | CommodityType.FROZEN     | Quantities.getQuantity(-10, Units.CELSIUS)
  }

  void "calculateVerifiedGrossMass() method should work as expected"() {
    given:
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, Quantities.getQuantity(1000, Units.KILOGRAM))

    when:
    ContainerInfo containerInfo = ContainerInfo.create(containerTypeParam, containerCommodityInfo)

    then:
    containerInfo.calculateVerifiedGrossMass() == verifiedGrossMassParam

    where:
    containerTypeParam                          | commodityTypeParam    | verifiedGrossMassParam
    ContainerType.TYPE_ISO_22G1                 | CommodityType.DRY     | Quantities.getQuantity(3300, Units.KILOGRAM)
    ContainerType.TYPE_ISO_42G1                 | CommodityType.DRY     | Quantities.getQuantity(4750, Units.KILOGRAM)
    ContainerType.TYPE_ISO_22R1_STANDARD_REEFER | CommodityType.CHILLED | Quantities.getQuantity(4080, Units.KILOGRAM)
    ContainerType.TYPE_ISO_42R1_STANDARD_REEFER | CommodityType.CHILLED | Quantities.getQuantity(5800, Units.KILOGRAM)
  }
}
