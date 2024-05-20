/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.domain.model.value

import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.objenesis.ObjenesisHelper
import spock.lang.Specification
import tech.units.indriya.quantity.QuantityRange

import java.lang.reflect.Field

import static CommodityType.AIR_COOLED
import static CommodityType.CHILLED
import static CommodityType.DRY
import static CommodityType.FROZEN

class CommoditySpecification extends Specification {
  void "map constructor should work for correct requestedStorageTemperature param"() {
    when:
    Commodity commodity = new Commodity(commodityType: commodityTypeParam, weight: 1.kg, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    commodity
    commodity.requestedStorageTemperature == requestedStorageTemperatureParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null

    AIR_COOLED         | 2.degC
    AIR_COOLED         | 8.degC
    AIR_COOLED         | 12.degC

    CHILLED            | -2.degC
    CHILLED            | 3.degC
    CHILLED            | 6.degC

    FROZEN             | -20.degC
    FROZEN             | -15.degC
    FROZEN             | -8.degC
  }

  void "map constructor should work for correct weight param"() {
    when:
    Commodity commodity = new Commodity(commodityType: DRY, weight: weightParam, requestedStorageTemperature: null)

    then:
    commodity
    commodity.weight.value.toBigDecimal() == weightValueParam

    where:
    weightParam    | weightValueParam
    1.kg           | 1G
    1000.kg        | 1000G
    1000.0.kg      | 1000G
    1000.000000.kg | 1000G
  }

  void "map constructor should fail for null input params"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: weightParam, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    commodityTypeParam | weightParam | requestedStorageTemperatureParam
    null               | 1.kg        | 1.degC
    DRY                | null        | 1.degC
  }

  void "map constructor should fail when weight is less than one kilogram"() {
    when:
    new Commodity(commodityType: DRY, weight: weightParam, requestedStorageTemperature: 1.degC)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: 1.kg.isLessThanOrEqualTo(weight)]")

    where:
    weightParam | _
    0.kg        | _
    0.1.kg      | _
    -1.kg       | _
  }

  void "map constructor should fail when weight is not in kilograms"() {
    when:
    new Commodity(commodityType: DRY, weight: 1500.g, requestedStorageTemperature: 1.degC)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.unit == Units.KILOGRAM)]"
  }

  void "map constructor should fail when weight value is not a whole number"() {
    when:
    new Commodity(commodityType: DRY, weight: 10.5.kg, requestedStorageTemperature: 1.degC)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Require violation detected - boolean condition is false - [condition: (weight.value.toBigDecimal().scale() == 0)]"
  }

  void "map constructor should fail for non-null requestedStorageTemperature for commodity types that do not support storage temperature"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: 1.kg, requestedStorageTemperature: 1.degC)

    then:
    DomainException domainException = thrown()
    domainException.message == "Bad Request"
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == "commodity.requestedStorageTemperatureNotAllowedForCommodityType"
    domainException.violationInfo.violationCode.resolvableMessageParameters == resolvableMessageParametersParam

    where:
    commodityTypeParam | resolvableMessageParametersParam
    DRY                | ["DRY"]
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: 1.kg, requestedStorageTemperature: null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureAvailableWhenNeeded(requestedStorageTemperature, commodityType)]")

    where:
    commodityTypeParam | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }

  void "map constructor should fail when requestedStorageTemperature is not in Celsius"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: 1.kg, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: this.isRequestedStorageTemperatureExpressedInCelsius(requestedStorageTemperature)]")

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    AIR_COOLED         | 35.6.degF
    CHILLED            | 28.4.degF
    FROZEN             | -4.degF
  }

  void "map constructor should fail for requestedStorageTemperature not in required range"() {
    when:
    new Commodity(commodityType: commodityTypeParam, weight: 1.kg, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.resolvableMessageKey == resolvableMessageKeyParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | resolvableMessageKeyParam
    AIR_COOLED         | 1.degC                           | "commodity.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED         | 13.degC                          | "commodity.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED            | -3.degC                          | "commodity.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED            | 7.degC                           | "commodity.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN             | -21.degC                         | "commodity.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN             | -7.degC                          | "commodity.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
  }

  // Here we have an example of adding non existing value to the enum for testing "impossible" switch default cases.
  // Adapted from https://stackoverflow.com/questions/5323505/mocking-java-enum-to-add-a-value-to-test-fail-case/57825724#57825724
  //
  // Requires adding "--add-opens java.base/java.lang=ALL-UNNAMED" JVM argument (see test configuration in cargotracking-domain-model-value/build.gradle)
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
      it("recommendedStorageTemperature").set(nonExistentEnumValue, 6.degC)
      it("storageTemperatureRange").set(nonExistentEnumValue, QuantityRange.of(2.degC, 12.degC))
    }

    when:
    new Commodity(commodityType: nonExistentEnumValue, weight: 1.kg, requestedStorageTemperature: 50.degC)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Unexpected CommodityType value: [value: NON_EXISTENT_ENUM_VALUE]"
  }

  void "make(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, 1.kg, requestedStorageTemperatureParam)

    then:
    commodity
    commodity.requestedStorageTemperature == expectedRequestedStorageTemperatureParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | expectedRequestedStorageTemperatureParam
    DRY                | null                             | null

    AIR_COOLED         | 2.degC                           | 2.degC
    AIR_COOLED         | 35.6.degF                        | 2.degC
    AIR_COOLED         | 7.degC                           | 7.degC
    AIR_COOLED         | 44.6.degF                        | 7.degC
    AIR_COOLED         | 12.degC                          | 12.degC
    AIR_COOLED         | 53.6.degF                        | 12.degC

    AIR_COOLED         | 36.degF                          | 2.22.degC
    AIR_COOLED         | 44.degF                          | 6.67.degC
    AIR_COOLED         | 53.degF                          | 11.67.degC

    CHILLED            | -2.degC                          | -2.degC
    CHILLED            | 28.4.degF                        | -2.degC
    CHILLED            | 0.degC                           | 0.degC
    CHILLED            | 32.degF                          | 0.degC
    CHILLED            | 6.degC                           | 6.degC
    CHILLED            | 42.8.degF                        | 6.degC

    CHILLED            | 29.degF                          | -1.67.degC
    CHILLED            | 31.degF                          | -0.56.degC
    CHILLED            | 42.degF                          | 5.56.degC

    FROZEN             | -20.degC                         | -20.degC
    FROZEN             | -4.degF                          | -20.degC
    FROZEN             | -15.degC                         | -15.degC
    FROZEN             | 5.degF                           | -15.degC
    FROZEN             | -8.degC                          | -8.degC
    FROZEN             | 17.6.degF                        | -8.degC

    FROZEN             | -3.degF                          | -19.44.degC
    FROZEN             | 0.degF                           | -17.78.degC
    FROZEN             | 17.degF                          | -8.33.degC
  }

  void "make(CommodityType, Quantity, Quantity) factory method should acquire recommendedStorageTemperature when requestedStorageTemperature is not given"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, 1.kg, null)

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
    Commodity.make(null, 1.kg, null)

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
    weightParam | weightValueParam
    1500.g      | 2
    1.5.kg      | 2
    1.001.kg    | 2
    1.t         | 1000
    1.1.t       | 1100
    1.11.t      | 1110
    1.111.t     | 1111
    1.1111.t    | 1112
    3.lb        | 2
    10.lb       | 5
    100.lb      | 46
  }

  void "make(CommodityType, Quantity) factory method should work for correct input params"() {
    when:
    Commodity commodity = Commodity.make(commodityTypeParam, 1.kg)

    then:
    commodity
    commodity.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | 6.degC
    CHILLED            | 0.degC
    FROZEN             | -12.degC
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

    AIR_COOLED         | 2                                | requestedStorageTemperatureParam.degC
    AIR_COOLED         | 8                                | requestedStorageTemperatureParam.degC
    AIR_COOLED         | 12                               | requestedStorageTemperatureParam.degC

    CHILLED            | -2                               | requestedStorageTemperatureParam.degC
    CHILLED            | 3                                | requestedStorageTemperatureParam.degC
    CHILLED            | 6                                | requestedStorageTemperatureParam.degC

    FROZEN             | -20                              | requestedStorageTemperatureParam.degC
    FROZEN             | -15                              | requestedStorageTemperatureParam.degC
    FROZEN             | -8                               | requestedStorageTemperatureParam.degC
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
    AIR_COOLED         | 6.degC
    CHILLED            | 0.degC
    FROZEN             | -12.degC
  }
}
