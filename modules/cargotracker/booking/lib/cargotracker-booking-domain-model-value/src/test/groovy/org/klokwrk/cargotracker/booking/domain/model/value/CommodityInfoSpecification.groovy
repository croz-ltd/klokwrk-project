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
    CommodityInfo commodityInfo = new CommodityInfo(commodityType: commodityTypeParam, weight: oneKilogram, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == requestedStorageTemperatureParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam
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

  void "map constructor should work for correct weight param"() {
    when:
    CommodityInfo commodityInfo = new CommodityInfo(commodityType: DRY, weight: weightParam, requestedStorageTemperature: null)

    then:
    commodityInfo
    commodityInfo.weight.value.toBigDecimal() == weightValueParam

    where:
    weightParam                        | weightValueParam
    getQuantity(1, KILOGRAM)           | 1G
    getQuantity(1000, KILOGRAM)        | 1000G
    getQuantity(1000.0, KILOGRAM)      | 1000G
    getQuantity(1000.000000, KILOGRAM) | 1000G
  }

  void "map constructor should fail for null input params"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, weight: weightParam, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    commodityTypeParam | weightParam              | requestedStorageTemperatureParam
    null               | getQuantity(1, KILOGRAM) | getQuantity(1, CELSIUS)
    DRY                | null                     | getQuantity(1, CELSIUS)
  }

  void "map constructor should fail when weight is less than one kilogram"() {
    when:
    new CommodityInfo(commodityType: DRY, weight: getQuantity(weightValueParam, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(weight)]")

    where:
    weightValueParam | _
    0                | _
    0.1              | _
    -1               | _
  }

  void "map constructor should fail when weight is not in kilograms"() {
    when:
    new CommodityInfo(commodityType: DRY, weight: getQuantity(1500, GRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.unit == Units.KILOGRAM)]"
  }

  void "map constructor should fail when weight value is not a whole number"() {
    when:
    new CommodityInfo(commodityType: DRY, weight: getQuantity(10.5, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.value.toBigDecimal().scale() == 0)]"
  }

  void "map constructor should fail for non-null requestedStorageTemperature for commodity types that do not support storage temperature"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.message == "Bad Request"
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == "commodityInfo.requestedStorageTemperatureNotAllowedForCommodityType"
    domainException.violationInfo.violationCode.resolvableMessageParameters == resolvableMessageParametersParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | resolvableMessageParametersParam
    DRY                | getQuantity(1, CELSIUS)          | ["DRY"]
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

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
    new CommodityInfo(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | resolvableMessageKeyParam
    AIR_COOLED         | getQuantity(1, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED         | getQuantity(13, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED            | getQuantity(-3, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED            | getQuantity(7, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN             | getQuantity(-21, CELSIUS)        | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN             | getQuantity(-7, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
  }

  // Here we have an example of adding non existing value to the enum for testing "impossible" switch default cases.
  // Adapted from https://stackoverflow.com/questions/5323505/mocking-java-enum-to-add-a-value-to-test-fail-case/57825724#57825724
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
    new CommodityInfo(commodityType: nonExistentEnumValue, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: getQuantity(50, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Unexpected CommodityType value: [value: NON_EXISTENT_ENUM_VALUE]"
  }

  void "make(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(commodityTypeParam, oneKilogram, requestedStorageTemperatureParam)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == requestedStorageTemperatureParam

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

  void "make(CommodityType, Quantity, Quantity) factory method should acquire recommendedStorageTemperature when requestedStorageTemperature is not given"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(commodityTypeParam, oneKilogram, null)

    then:
    commodityInfo.requestedStorageTemperature == commodityTypeParam.recommendedStorageTemperature

    where:
    commodityTypeParam | _
    DRY                | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }

  void "make(CommodityType, Quantity, Quantity) factory method should skip requiring recommendedStorageTemperature for the combination of invalid input params"() {
    when:
    CommodityInfo.make(null, oneKilogram, null)

    then:
    thrown(AssertionError)
  }

  void "make(CommodityType, Quantity, Quantity) factory method should convert weight in kilograms and round it up to the whole number"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(DRY, weightParam, null)

    then:
    commodityInfo
    commodityInfo.weight.value == weightValueParam

    where:
    weightParam                  | weightValueParam
    getQuantity(1500, GRAM)      | 2
    getQuantity(1.5, KILOGRAM)   | 2
    getQuantity(1.001, KILOGRAM) | 2
  }

  void "make(CommodityType, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(commodityTypeParam, oneKilogram)

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

  void "make(CommodityType, Integer, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(commodityTypeParam, 1, requestedStorageTemperatureParam)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == requestedStorageTemperatureExpectedParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | requestedStorageTemperatureExpectedParam
    DRY                | null                             | requestedStorageTemperatureParam

    AIR_COOLED         | 2                                | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    AIR_COOLED         | 8                                | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    AIR_COOLED         | 12                               | getQuantity(requestedStorageTemperatureParam, CELSIUS)

    CHILLED            | -2                               | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    CHILLED            | 3                                | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    CHILLED            | 6                                | getQuantity(requestedStorageTemperatureParam, CELSIUS)

    FROZEN             | -20                              | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    FROZEN             | -15                              | getQuantity(requestedStorageTemperatureParam, CELSIUS)
    FROZEN             | -8                               | getQuantity(requestedStorageTemperatureParam, CELSIUS)
  }

  void "make(CommodityType, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.make(commodityTypeParam, 1)

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
