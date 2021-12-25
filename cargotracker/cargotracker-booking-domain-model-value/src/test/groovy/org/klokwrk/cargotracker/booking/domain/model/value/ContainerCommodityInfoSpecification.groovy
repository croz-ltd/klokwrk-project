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

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.AIR_COOLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.CHILLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.FROZEN
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.CELSIUS
import static tech.units.indriya.unit.Units.KILOGRAM

class ContainerCommodityInfoSpecification extends Specification {
  static Quantity<Mass> oneKilogram = getQuantity(1, KILOGRAM)

  void "map constructor should work for correct input params"() {
    when:
    ContainerCommodityInfo containerCommodityInfo = new ContainerCommodityInfo(
        commodityType: commodityTypeParam, commodityInContainerWeight: oneKilogram, commodityRequestedStorageTemperature: requestedStorageTemperatureParam
    )

    then:
    containerCommodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | getQuantity(25, CELSIUS)
    DRY                | null

    AIR_COOLED         | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(8, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)

    CHILLED            | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(3, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)

    FROZEN             | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)
  }

  void "map constructor should fail for invalid null input params"() {
    when:
    new ContainerCommodityInfo(commodityType: commodityTypeParam, commodityInContainerWeight: totalWeightParam, commodityRequestedStorageTemperature: null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    commodityTypeParam | totalWeightParam
    null               | getQuantity(1, KILOGRAM)
    DRY                | null
  }

  void "map constructor should fail for invalid totalWeight param"() {
    when:
    new ContainerCommodityInfo(commodityType: DRY, commodityInContainerWeight: getQuantity(totalWeightValueParam, KILOGRAM), commodityRequestedStorageTemperature: null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(commodityInContainerWeight)]")

    where:
    totalWeightValueParam | _
    0                     | _
    -1                    | _
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new ContainerCommodityInfo(commodityType: commodityTypeParam, commodityInContainerWeight: getQuantity(1, KILOGRAM), commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureAvailableWhenNeeded(commodityRequestedStorageTemperature, commodityType)]")

    where:
    commodityTypeParam | commodityRequestedStorageTemperatureParam
    AIR_COOLED         | null
    CHILLED            | null
    FROZEN             | null
  }

  void "map constructor should fail for requestedStorageTemperature not in required range"() {
    when:
    new ContainerCommodityInfo(commodityType: commodityTypeParam, commodityInContainerWeight: getQuantity(1, KILOGRAM), commodityRequestedStorageTemperature: commodityRequestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureInAllowedRange(commodityRequestedStorageTemperature, commodityType)]")

    where:
    commodityTypeParam | commodityRequestedStorageTemperatureParam
    AIR_COOLED         | getQuantity(1, CELSIUS)
    AIR_COOLED         | getQuantity(13, CELSIUS)

    CHILLED            | getQuantity(-3, CELSIUS)
    CHILLED            | getQuantity(7, CELSIUS)

    FROZEN             | getQuantity(-21, CELSIUS)
    FROZEN             | getQuantity(-7, CELSIUS)
  }

  void "create(CommodityType, Quantity, Quantity) method should work for correct input params"() {
    when:
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, oneKilogram, requestedStorageTemperatureParam)

    then:
    containerCommodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null
    DRY                | getQuantity(25, CELSIUS)

    AIR_COOLED         | null
    AIR_COOLED         | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(8, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)

    CHILLED            | null
    CHILLED            | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(3, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)

    FROZEN             | null
    FROZEN             | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)
  }

  void "create(CommodityType, Quantity, Quantity) method should fail for invalid input params"() {
    when:
    ContainerCommodityInfo.create(null, null, null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("matcher does not match - [item: commodityType, expected: notNullValue(), actual: null]")
  }

  void "create(CommodityType, Quantity) method should work for correct input params"() {
    when:
    ContainerCommodityInfo containerCommodityInfo = ContainerCommodityInfo.create(commodityTypeParam, oneKilogram)

    then:
    containerCommodityInfo

    where:
    commodityTypeParam | _
    DRY                | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }
}
