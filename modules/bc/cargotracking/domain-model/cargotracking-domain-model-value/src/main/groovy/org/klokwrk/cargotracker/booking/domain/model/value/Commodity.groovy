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
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature
import java.math.RoundingMode

import static org.hamcrest.Matchers.notNullValue

/**
 * The commodity of a cargo.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class Commodity implements PostMapConstructorCheckable {
  /**
   * Type of commodity.
   * <p/>
   * Must not be {@code null}.
   */
  CommodityType commodityType

  /**
   * The weight of commodity.
   * <p/>
   * This weight might exceed what a single container can carry (in that case, multiple containers will be allocated based on this commodity).
   * <p/>
   * Must not be {@code null}, and must be at least 1 kg or greater. It must be specified in kilogram units and a value must be the whole number.
   * For conversion from other {@code Mass} units, and for a conversion of decimal numbers, use {@code make()} factory methods.
   */
  Quantity<Mass> weight

  /**
   * Requested storage temperature for a commodity.
   * <p/>
   * Whether it is necessary depends on the commodity type. If commodity type supports storage temperature, {@code requestedStorageTemperature} must be inside of temperature range boundaries of the
   * corresponding commodity type.
   * <p/>
   * When using factory {@code make()} methods, if not provided, the requested storage temperature is populated from the recommended storage temperature of the corresponding commodity type that
   * supports storage temperature.
   * <p/>
   * Map constructor will throw an {@code DomainException} when {@code requestedStorageTemperature} is non-null and {@code commodityType} does not support storage temperature
   * ({@code commodityType.containerFeaturesType.isContainerTemperatureControlled()} returns {@code false}).
   */
  Quantity<Temperature> requestedStorageTemperature

  static Commodity make(CommodityType commodityType, Quantity<Mass> weight) {
    Commodity commodity = make(commodityType, weight, null)
    return commodity
  }

  /**
   * The main factory method for creating {@code Commodity} instance (all other {@code make()} factory methods delegate to this one).
   * <p/>
   * The only optional parameter (can be provided as {@code null}) is {@code requestedStorageTemperature}. When {@code null}, the actual {code requestedStorageTemperature} of the instance is set to
   * the {@code recommendedStorageTemperature} of provided {@code commodityType}. Note that the {@code recommendedStorageTemperature} of {@code commodityType} can be {@code null}.
   */
  static Commodity make(CommodityType commodityType, Quantity<Mass> weight, Quantity<Temperature> requestedStorageTemperature) {
    Quantity<Temperature> requestedStorageTemperatureToUse = requestedStorageTemperature
    if (commodityType != null && commodityType.isStorageTemperatureLimited() && requestedStorageTemperature == null) {
      requestedStorageTemperatureToUse = commodityType.recommendedStorageTemperature
    }

    if (commodityType?.isStorageTemperatureLimited()) {
      if (requestedStorageTemperature == null) {
        requestedStorageTemperatureToUse = commodityType.recommendedStorageTemperature
      }
      else {
        BigDecimal requestedStorageTemperatureValueToUse = requestedStorageTemperature.to(Units.CELSIUS).value.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        requestedStorageTemperatureToUse = requestedStorageTemperatureValueToUse.degC
      }
    }

    BigDecimal weightValueToUse = weight.to(Units.KILOGRAM).value.toBigDecimal().setScale(0, RoundingMode.UP)
    Quantity<Mass> weightToUse = weightValueToUse.kg

    Commodity commodity = new Commodity(commodityType: commodityType, weight: weightToUse, requestedStorageTemperature: requestedStorageTemperatureToUse)
    return commodity
  }

  static Commodity make(CommodityType commodityType, Long weightInKilograms) {
    Commodity commodity = make(commodityType, weightInKilograms, null)
    return commodity
  }

  static Commodity make(CommodityType commodityType, Long weightInKilograms, Integer requestedStorageTemperatureDegC) {
    Commodity commodity = make(
        commodityType,
        weightInKilograms.kg,
        requestedStorageTemperatureDegC == null ? null : requestedStorageTemperatureDegC.degC
    )

    return commodity
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(commodityType, notNullValue())
    requireMatch(weight, notNullValue())

    requireTrue(1.kg.isLessThanOrEqualTo(weight))
    requireTrue(weight.unit == Units.KILOGRAM)
    requireTrue(weight.value.toBigDecimal().scale() == 0)

    requireNullRequestedStorageTemperatureWhenNotAllowed(requestedStorageTemperature, commodityType)

    requireTrue(isRequestedStorageTemperatureAvailableWhenNeeded(requestedStorageTemperature, commodityType))
    requireTrue(isRequestedStorageTemperatureExpressedInCelsius(requestedStorageTemperature))
    requireRequestedStorageTemperatureInAllowedRange(requestedStorageTemperature, commodityType)
  }

  private void requireNullRequestedStorageTemperatureWhenNotAllowed(Quantity<Temperature> requestedStorageTemperature, CommodityType commodityType) {
    if (requestedStorageTemperature != null && !commodityType.containerFeaturesType.isContainerTemperatureControlled()) {
      String messageKey = "commodity.requestedStorageTemperatureNotAllowedForCommodityType"
      List<String> messageParams = [commodityType.name()]
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey(messageKey, messageParams))
    }
  }

  private Boolean isRequestedStorageTemperatureAvailableWhenNeeded(Quantity<Temperature> requestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.containerFeaturesType.isContainerTemperatureControlled() && requestedStorageTemperature == null) {
      return false
    }

    return true
  }

  private Boolean isRequestedStorageTemperatureExpressedInCelsius(Quantity<Temperature> requestedStorageTemperature) {
    // Should be called after isRequestedStorageTemperatureAvailableWhenNeeded().
    if (requestedStorageTemperature != null && requestedStorageTemperature.unit != Units.CELSIUS) {
      return false
    }

    return true
  }

  private void requireRequestedStorageTemperatureInAllowedRange(Quantity<Temperature> requestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.isStorageTemperatureLimited() && (!commodityType.isStorageTemperatureAllowed(requestedStorageTemperature))) {
      String messageKey
      switch (commodityType) {
        case CommodityType.AIR_COOLED:
          messageKey = "commodity.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
          break
        case CommodityType.CHILLED:
          messageKey = "commodity.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
          break
        case CommodityType.FROZEN:
          messageKey = "commodity.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
          break
        default:
          // As we are switching over enum values, just make sure that we are not missing some of them.
          throw new AssertionError("Unexpected CommodityType value: [value: ${ commodityType.name() }]", null)
      }

      String minRangeBound = commodityType.storageTemperatureRange.minimum.value.toInteger()
      String maxRangeBound = commodityType.storageTemperatureRange.maximum.value.toInteger()
      List<String> messageParams = [minRangeBound, maxRangeBound]

      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey(messageKey, messageParams))
    }
  }
}
