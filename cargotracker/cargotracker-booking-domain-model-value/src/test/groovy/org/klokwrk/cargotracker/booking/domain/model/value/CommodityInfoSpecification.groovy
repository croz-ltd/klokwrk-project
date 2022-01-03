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

import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.objenesis.ObjenesisHelper
import spock.lang.Specification
import tech.units.indriya.quantity.QuantityRange

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.lang.reflect.Field

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.CHILLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.AIR_COOLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.FROZEN
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.CELSIUS
import static tech.units.indriya.unit.Units.GRAM
import static tech.units.indriya.unit.Units.KILOGRAM

class CommodityInfoSpecification extends Specification {
  static Quantity<Mass> oneKilogram = getQuantity(1, KILOGRAM)

  void "map constructor should work for correct requestedStorageTemperature param"() {
    when:
    CommodityInfo commodityInfo = new CommodityInfo(commodityType: commodityTypeParam, totalWeight: oneKilogram, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    commodityInfo

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

  void "map constructor should work for correct totalWeight param"() {
    when:
    CommodityInfo commodityInfo = new CommodityInfo(commodityType: DRY, totalWeight: totalWeightParam, requestedStorageTemperature: null)

    then:
    commodityInfo
    commodityInfo.totalWeight.value.toBigDecimal() == totalWeightValueParam

    where:
    totalWeightParam                   | totalWeightValueParam
    getQuantity(1, KILOGRAM)           | 1G
    getQuantity(1000, KILOGRAM)        | 1000G
    getQuantity(1000.0, KILOGRAM)      | 1000G
    getQuantity(1000.000000, KILOGRAM) | 1000G
  }

  void "map constructor should fail for null input params"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: totalWeightParam, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    commodityTypeParam | totalWeightParam         | requestedStorageTemperatureParam
    null               | getQuantity(1, KILOGRAM) | getQuantity(1, CELSIUS)
    DRY                | null                     | getQuantity(1, CELSIUS)
  }

  void "map constructor should fail when totalWeight is less than one kilogram"() {
    when:
    new CommodityInfo(commodityType: DRY, totalWeight: getQuantity(totalWeightValueParam, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(totalWeight)]")

    where:
    totalWeightValueParam | _
    0                     | _
    0.1                   | _
    -1                    | _
  }

  void "map constructor should fail when totalWeight is not in kilograms"() {
    when:
    new CommodityInfo(commodityType: DRY, totalWeight: getQuantity(1500, GRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (totalWeight.unit == Units.KILOGRAM)]"
  }

  void "map constructor should fail when totalWeight value is not a whole number"() {
    when:
    new CommodityInfo(commodityType: DRY, totalWeight: getQuantity(10.5, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (totalWeight.value.toBigDecimal().scale() == 0)]"
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureAvailableWhenNeeded(requestedStorageTemperature, commodityType)]")

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    AIR_COOLED         | null
    CHILLED            | null
    FROZEN             | null
  }

  void "map constructor should fail for requestedStorageTemperature not in required range"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | violationCodeKeyParam
    AIR_COOLED         | getQuantity(1, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED         | getQuantity(13, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED            | getQuantity(-3, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED            | getQuantity(7, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN             | getQuantity(-21, CELSIUS)        | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN             | getQuantity(-7, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
  }

  // adapted from https://stackoverflow.com/questions/5323505/mocking-java-enum-to-add-a-value-to-test-fail-case/57825724#57825724
  void "map constructor requestedStorageTemperature check should fail for non existing commodityType"() {
    given:
    Closure<Field> makeAccessibleField = { Class clazz, String fieldName ->
      Field result = clazz.getDeclaredField(fieldName)
      result.accessible = true
      return result
    }

    CommodityType nonExistentEnumValue = ObjenesisHelper.newInstance(CommodityType)
    makeAccessibleField.curry(Enum).with {
      it("name").set(nonExistentEnumValue, "NON_EXISTENT_ENUM_VALUE")
    }

    makeAccessibleField.curry(CommodityType).with {
      it("containerFeaturesType").set(nonExistentEnumValue, ContainerFeaturesType.FEATURES_ISO_R1_STANDARD_REEFER)
      it("recommendedStorageTemperature").set(nonExistentEnumValue, getQuantity(6, CELSIUS))
      it("storageTemperatureRange").set(nonExistentEnumValue, QuantityRange.of(getQuantity(2, CELSIUS), getQuantity(12, CELSIUS)))
    }

    when:
    new CommodityInfo(commodityType: nonExistentEnumValue, totalWeight: getQuantity(1, KILOGRAM), requestedStorageTemperature: getQuantity(50, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Unexpected CommodityType value: [value: NON_EXISTENT_ENUM_VALUE]"
  }

  void "create(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram, requestedStorageTemperatureParam)

    then:
    commodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null

    AIR_COOLED         | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(7, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)

    CHILLED            | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)

    FROZEN             | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)
  }

  void "create(CommodityType, Quantity, Quantity) factory method should acquire recommendedStorageTemperature when requestedStorageTemperature is not given"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram, null)

    then:
    commodityInfo.requestedStorageTemperature == commodityTypeParam.recommendedStorageTemperature

    where:
    commodityTypeParam | _
    DRY                | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }

  void "create(CommodityType, Quantity, Quantity) factory method should skip requiring recommendedStorageTemperature for the combination of invalid input params"() {
    when:
    CommodityInfo.create(null, oneKilogram, null)

    then:
    thrown(AssertionError)
  }

  void "create(CommodityType, Quantity, Quantity) factory method should convert totalWeight in kilograms and round it up to the whole number"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(DRY, totalWeightParam, null)

    then:
    commodityInfo
    commodityInfo.totalWeight.value == 2

    where:
    totalWeightParam             | totalWeightValueParam
    getQuantity(1500, GRAM)      | 2
    getQuantity(1.5, KILOGRAM)   | 2
    getQuantity(1.001, KILOGRAM) | 2
  }

  void "create(CommodityType, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }

  void "create(CommodityType, Integer, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, 1, requestedStorageTemperatureParam)

    then:
    commodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null

    AIR_COOLED         | 2
    AIR_COOLED         | 8
    AIR_COOLED         | 12

    CHILLED            | -2
    CHILLED            | 3
    CHILLED            | 6

    FROZEN             | -20
    FROZEN             | -15
    FROZEN             | -8
  }

  void "create(CommodityType, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, 1)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }
}
