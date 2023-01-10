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

import spock.lang.Specification

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_12G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22G1
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
import static org.klokwrk.cargotracker.booking.domain.model.value.ContainerType.TYPE_ISO_42G1
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.GRAM
import static tech.units.indriya.unit.Units.KILOGRAM

class CargoSpecification extends Specification {

  void "map constructor should work for correct parameters"() {
    when:
    Cargo cargo = new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 2_000),
        maxAllowedWeightPerContainer: getQuantity(2_200, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(2_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    cargo
  }

  void "map constructor should fail for invalid combination of containerType and commodity"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22R1_STANDARD_REEFER, // It should be TYPE_ISO_22G1, for example.
        commodity: Commodity.make(DRY, 2_000),
        maxAllowedWeightPerContainer: getQuantity(2_200, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(2_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (containerType.featuresType == commodity.commodityType.containerFeaturesType)]")
  }

  void "map constructor should fail for invalid combination of containerType and maxAllowedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(30_000, KILOGRAM), // It should be <= containerType.maxCommodityWeight.
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: containerType.maxCommodityWeight.toComparable().isGreaterThanOrEqualTo(maxAllowedWeightPerContainer)]")
  }

  void "map constructor should fail for invalid combination of commodity and maxRecommendedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(19_000, KILOGRAM), // It should be >= commodity.totalWeight.
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("[condition: ((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= commodity.weight.value.toBigDecimal())]")
  }

  void "map constructor should fail for invalid combination of commodity and containerCount"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 50_000),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 2, // It should be 3 or more.
        containerTeuCount: 2
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("[condition: ((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= commodity.weight.value.toBigDecimal())]")
  }

  void "map constructor should fail for invalid units of maxAllowedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000_000, GRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxAllowedWeightPerContainer.unit == Units.KILOGRAM)]")
  }

  void "map constructor should fail for invalid values of maxAllowedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000.1, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxAllowedWeightPerContainer.value.toBigDecimal().scale() == 0)]")
  }

  void "map constructor should fail for invalid units of maxRecommendedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000_000, GRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxRecommendedWeightPerContainer.unit == Units.KILOGRAM)]")
  }

  void "map constructor should fail for invalid values of maxRecommendedWeightPerContainer"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000.1, KILOGRAM),
        containerCount: 1,
        containerTeuCount: 1
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - [condition: (maxRecommendedWeightPerContainer.value.toBigDecimal().scale() == 0)]")
  }

  void "map constructor should fail for invalid value of containerTeuCount"() {
    when:
    new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, 20_000),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(20_000, KILOGRAM),
        containerCount: 1,
        containerTeuCount: containerTeuCountParam
    )

    then:
    AssertionError assertionError = thrown()
    assertionError.message.endsWith("boolean condition is false - $messageEndParam")

    where:
    containerTeuCountParam | messageEndParam
    1.001                  | "[condition: (containerTeuCount.scale() <= 2)]"
    new BigDecimal(1G, -3) | "[condition: (containerTeuCount.scale() >= 0)]"
    1.5                    | "[condition: (containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))]"
    1.01                   | "[condition: (containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))]"
    0.5                    | "[condition: (containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))]"
    0.91                   | "[condition: (containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))]"
    0.99                   | "[condition: (containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))]"
  }

  void "make() method should work as expected for standard 10ft container"() {
    given:
    Cargo expectedCargo = new Cargo(
        containerType: TYPE_ISO_12G1,
        commodity: Commodity.make(DRY, commodityWeightInKilogramsParam),
        maxAllowedWeightPerContainer: getQuantity(9_500, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(maxRecommendedWeightPerContainerParam, KILOGRAM),
        containerCount: containerCountParam,
        containerTeuCount: containerTeuCountParam
    )

    when:
    Cargo actualCargo = Cargo.make(TYPE_ISO_12G1, Commodity.make(DRY, commodityWeightInKilogramsParam), getQuantity(9_500, KILOGRAM))

    then:
    expectedCargo == actualCargo

    where:
    commodityWeightInKilogramsParam | maxRecommendedWeightPerContainerParam | containerCountParam | containerTeuCountParam
    2_000                           | 2_000                                 | 1                   | 0.5
    10_000                          | 5_000                                 | 2                   | 1
    50_000                          | 8_334                                 | 6                   | 3
    500_000                         | 9_434                                 | 53                  | 26.50
//    5_000_000                       | 9_488                                 | 527                 | 263.50
    12_345_678                      | 9_497                                 | 1300                | 650
  }

  void "make() method should work as expected for standard 20ft container"() {
    given:
    Cargo expectedCargo = new Cargo(
        containerType: TYPE_ISO_22G1,
        commodity: Commodity.make(DRY, commodityWeightInKilogramsParam),
        maxAllowedWeightPerContainer: getQuantity(21_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(maxRecommendedWeightPerContainerParam, KILOGRAM),
        containerCount: containerCountParam,
        containerTeuCount: containerTeuCountParam
    )

    when:
    Cargo actualCargo = Cargo.make(TYPE_ISO_22G1, Commodity.make(DRY, commodityWeightInKilogramsParam), getQuantity(21_000, KILOGRAM))

    then:
    expectedCargo == actualCargo

    where:
    commodityWeightInKilogramsParam | maxRecommendedWeightPerContainerParam | containerCountParam | containerTeuCountParam
    2_000                           | 2_000                                 | 1                   | 1
    10_000                          | 10_000                                | 1                   | 1
    50_000                          | 16_667                                | 3                   | 3
    500_000                         | 20_834                                | 24                  | 24
  }

  void "make() method should work as expected for standard 40ft container"() {
    given:
    Cargo expectedCargo = new Cargo(
        containerType: TYPE_ISO_42G1,
        commodity: Commodity.make(DRY, commodityWeightInKilogramsParam),
        maxAllowedWeightPerContainer: getQuantity(26_000, KILOGRAM),
        maxRecommendedWeightPerContainer: getQuantity(maxRecommendedWeightPerContainerParam, KILOGRAM),
        containerCount: containerCountParam,
        containerTeuCount: containerTeuCountParam
    )

    when:
    Cargo actualCargo = Cargo.make(TYPE_ISO_42G1, Commodity.make(DRY, commodityWeightInKilogramsParam), getQuantity(26_000, KILOGRAM))

    then:
    expectedCargo == actualCargo

    where:
    commodityWeightInKilogramsParam | maxRecommendedWeightPerContainerParam | containerCountParam | containerTeuCountParam
    2_000                           | 2_000                                 | 1                   | 2
    10_000                          | 10_000                                | 1                   | 2
    50_000                          | 25_000                                | 2                   | 4
    500_000                         | 25_000                                | 20                  | 40
  }
}
