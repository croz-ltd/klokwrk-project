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

import spock.lang.Specification

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.GRAM
import static tech.units.indriya.unit.Units.KILOGRAM

class CommoditySpecification extends Specification {

  void "map constructor should work for correct parameters"() {
    when:
    Commodity commodity = new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 2_000),
        maxAllowedWeightPerContainer: getQuantity(2_200, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(2_000, KILOGRAM),
        containerCount: 1
    )

    then:
    commodity
  }

  void "map constructor should fail for invalid combination of containerType and commodityInfo"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22R1_STANDARD_REEFER, // It should be TYPE_ISO_22G1, for example.
        commodityInfo: CommodityInfo.make(DRY, 2_000),
        maxAllowedWeightPerContainer: getQuantity(2_200, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(2_000, KILOGRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (containerType.featuresType == commodityInfo.commodityType.containerFeaturesType)]")
  }

  void "map constructor should fail for invalid combination of containerType and maxAllowedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(30_000, KILOGRAM), // It should be <= containerType.maxCommodityWeight.
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (ComparableQuantity) containerType.maxCommodityWeight.isGreaterThanOrEqualTo(maxAllowedWeightPerContainer)]")
  }

  void "map constructor should fail for invalid combination of commodityInfo and maxRecommendedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 23_000),
        maxAllowedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM), // It should be >= commodityInfo.totalWeight.
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("[condition: ((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= commodityInfo.totalWeight.value.toBigDecimal())]")
  }

  void "map constructor should fail for invalid combination of commodityInfo and containerCount"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 50_000),
        maxAllowedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        containerCount: 2 // It should be 3 or more.
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("[condition: ((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= commodityInfo.totalWeight.value.toBigDecimal())]")
  }

  void "map constructor should fail for invalid units of maxAllowedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(24_000_000, GRAM),
        maxRecommendedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxAllowedWeightPerContainer.unit == Units.KILOGRAM)]")
  }

  void "map constructor should fail for invalid values of maxAllowedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(24_000.1, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxAllowedWeightPerContainer.value.toBigDecimal().scale() == 0)]")
  }

  void "map constructor should fail for invalid units of maxRecommendedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(24_000_000, GRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxRecommendedWeightPerContainer.unit == Units.KILOGRAM)]")
  }

  void "map constructor should fail for invalid values of maxRecommendedWeightPerContainer"() {
    when:
    new Commodity(
        containerType: TYPE_ISO_22G1,
        commodityInfo: CommodityInfo.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(24_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000.1, KILOGRAM),
        containerCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxRecommendedWeightPerContainer.value.toBigDecimal().scale() == 0)]")
  }
}
