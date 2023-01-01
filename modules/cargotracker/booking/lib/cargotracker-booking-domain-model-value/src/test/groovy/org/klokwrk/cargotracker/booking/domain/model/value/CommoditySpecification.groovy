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

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.AIR_COOLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.CHILLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.FROZEN
import static si.uom.NonSI.TONNE
import static systems.uom.common.USCustomary.FAHRENHEIT
import static systems.uom.common.USCustomary.POUND
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.CELSIUS
import static tech.units.indriya.unit.Units.GRAM
import static tech.units.indriya.unit.Units.KILOGRAM

class CommoditySpecification extends Specification {
  static Quantity<Mass> oneKilogram = getQuantity(1, KILOGRAM)

  void "map constructor should work for correct requestedStorageTemperature param"() {
    when:
    Commodity commodity = new Commodity(commodityType: commodityTypeParam, weight: oneKilogram, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    commodity
    commodity.requestedStorageTemperature == requestedStorageTemperatureParam

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
    Commodity commodity = new Commodity(commodityType: DRY, weight: weightParam, requestedStorageTemperature: null)

    then:
    commodity
    commodity.weight.value.toBigDecimal() == weightValueParam

    where:
    weightParam                        | weightValueParam
    getQuantity(1, KILOGRAM)           | 1G
    getQuantity(1000, KILOGRAM)        | 1000G
    getQuantity(1000.0, KILOGRAM)      | 1000G
    getQuantity(1000.000000, KILOGRAM) | 1000G
  }

  void "map constructor should fail for null input params"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: weightParam, requestedStorageTemperature: requestedStorageTemperatureParam)

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
    new Commodity(commodityType: DRY, weight: getQuantity(weightValueParam, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

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
    new Commodity(commodityType: DRY, weight: getQuantity(1500, GRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.unit == Units.KILOGRAM)]"
  }

  void "map constructor should fail when weight value is not a whole number"() {
    when:
    new Commodity(commodityType: DRY, weight: getQuantity(10.5, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.value.toBigDecimal().scale() == 0)]"
  }

  void "map constructor should fail for non-null requestedStorageTemperature for commodity types that do not support storage temperature"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.message == "Bad Request"
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == "commodity.requestedStorageTemperatureNotAllowedForCommodityType"
    domainException.violationInfo.violationCode.resolvableMessageParameters == resolvableMessageParametersParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | resolvableMessageParametersParam
    DRY                | getQuantity(1, CELSIUS)          | ["DRY"]
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureAvailableWhenNeeded(requestedStorageTemperature, commodityType)]")

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    AIR_COOLED         | null
    CHILLED            | null
    FROZEN             | null
  }

  void "map constructor should fail when requestedStorageTemperature is not in Celsius"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureExpressedInCelsius(requestedStorageTemperature)]")

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    AIR_COOLED         | getQuantity(35.6, FAHRENHEIT)
    CHILLED            | getQuantity(28.4, FAHRENHEIT)
    FROZEN             | getQuantity(-4, FAHRENHEIT)
  }

  void "map constructor should fail for requestedStorageTemperature not in required range"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | resolvableMessageKeyParam
    AIR_COOLED         | getQuantity(1, CELSIUS)          | "commodity.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED         | getQuantity(13, CELSIUS)         | "commodity.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED            | getQuantity(-3, CELSIUS)         | "commodity.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED            | getQuantity(7, CELSIUS)          | "commodity.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN             | getQuantity(-21, CELSIUS)        | "commodity.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN             | getQuantity(-7, CELSIUS)         | "commodity.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
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
    new Commodity(commodityType: nonExistentEnumValue, weight: getQuantity(1, KILOGRAM), requestedStorageTemperature: getQuantity(50, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Unexpected CommodityType value: [value: NON_EXISTENT_ENUM_VALUE]"
  }

  void "make(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, oneKilogram, requestedStorageTemperatureParam)

    then:
    commodity
    commodity.requestedStorageTemperature == expectedRequestedStorageTemperatureParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | expectedRequestedStorageTemperatureParam
    DRY                | null                             | null

    AIR_COOLED         | getQuantity(2, CELSIUS)          | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(35.6, FAHRENHEIT)    | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(7, CELSIUS)          | getQuantity(7, CELSIUS)
    AIR_COOLED         | getQuantity(44.6, FAHRENHEIT)    | getQuantity(7, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)         | getQuantity(12, CELSIUS)
    AIR_COOLED         | getQuantity(53.6, FAHRENHEIT)    | getQuantity(12, CELSIUS)

    AIR_COOLED         | getQuantity(36, FAHRENHEIT)      | getQuantity(2.22, CELSIUS)
    AIR_COOLED         | getQuantity(44, FAHRENHEIT)      | getQuantity(6.67, CELSIUS)
    AIR_COOLED         | getQuantity(53, FAHRENHEIT)      | getQuantity(11.67, CELSIUS)

    CHILLED            | getQuantity(-2, CELSIUS)         | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(28.4, FAHRENHEIT)    | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)          | getQuantity(0, CELSIUS)
    CHILLED            | getQuantity(32, FAHRENHEIT)      | getQuantity(0, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)          | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(42.8, FAHRENHEIT)    | getQuantity(6, CELSIUS)

    CHILLED            | getQuantity(29, FAHRENHEIT)      | getQuantity(-1.67, CELSIUS)
    CHILLED            | getQuantity(31, FAHRENHEIT)      | getQuantity(-0.56, CELSIUS)
    CHILLED            | getQuantity(42, FAHRENHEIT)      | getQuantity(5.56, CELSIUS)

    FROZEN             | getQuantity(-20, CELSIUS)        | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-4, FAHRENHEIT)      | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)        | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(5, FAHRENHEIT)       | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)         | getQuantity(-8, CELSIUS)
    FROZEN             | getQuantity(17.6, FAHRENHEIT)    | getQuantity(-8, CELSIUS)

    FROZEN             | getQuantity(-3, FAHRENHEIT)      | getQuantity(-19.44, CELSIUS)
    FROZEN             | getQuantity(0, FAHRENHEIT)       | getQuantity(-17.78, CELSIUS)
    FROZEN             | getQuantity(17, FAHRENHEIT)      | getQuantity(-8.33, CELSIUS)
  }

  void "make(CommodityType, Quantity, Quantity) factory method should acquire recommendedStorageTemperature when requestedStorageTemperature is not given"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, oneKilogram, null)

    then:
    commodity.requestedStorageTemperature == commodityTypeParam.recommendedStorageTemperature

    where:
    commodityTypeParam | _
    DRY                | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }

  void "make(CommodityType, Quantity, Quantity) factory method should skip requiring recommendedStorageTemperature for the combination of invalid input params"() {
    when:
    Commodity.make(null, oneKilogram, null)

    then:
    thrown(AssertionError)
  }

  void "make(CommodityType, Quantity, Quantity) factory method should convert weight in kilograms and round it up to the whole number"() {
    when:
    Commodity commodity = Commodity.make(DRY, weightParam, null)

    then:
    commodity
    commodity.weight.value == weightValueParam

    where:
    weightParam                  | weightValueParam
    getQuantity(1500, GRAM)      | 2
    getQuantity(1.5, KILOGRAM)   | 2
    getQuantity(1.001, KILOGRAM) | 2
    getQuantity(1, TONNE)        | 1000
    getQuantity(1.1, TONNE)      | 1100
    getQuantity(1.11, TONNE)     | 1110
    getQuantity(1.111, TONNE)    | 1111
    getQuantity(1.1111, TONNE)   | 1112
    getQuantity(3, POUND)        | 2
    getQuantity(10, POUND)       | 5
    getQuantity(100, POUND)      | 46
  }

  void "make(CommodityType, Quantity) factory method should work for correct input params"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, oneKilogram)

    then:
    commodity
    commodity.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }

  void "make(CommodityType, Integer, Integer) factory method should work for correct input params"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, 1, requestedStorageTemperatureParam)

    then:
    commodity
    commodity.requestedStorageTemperature == requestedStorageTemperatureExpectedParam

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
    Commodity commodity = Commodity.make(commodityTypeParam, 1)

    then:
    commodity
    commodity.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }
}
