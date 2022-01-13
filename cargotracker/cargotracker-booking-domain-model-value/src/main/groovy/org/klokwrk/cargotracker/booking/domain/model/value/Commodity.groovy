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

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.math.MathContext
import java.math.RoundingMode

import static org.hamcrest.Matchers.notNullValue

/**
 * Encapsulates all attributes for a commodity, including the assigned container type.
 * <p/>
 * All attributes can be calculated from {@link ContainerDimensionType} and {@link CommodityInfo}, as demonstrated in {@code BookingOfferAggregate}.
 * <p/>
 * Attributes of {@code Quantity<Mass>} type must have {@code Units.KILOGRAM} unit and a whole number value.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class Commodity implements PostMapConstructorCheckable {
  /**
   * ContainerType associated with this Commodity.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be {@code containerType.featuresType == commodityInfo.commodityType.containerFeaturesType}.<br/>
   */
  ContainerType containerType

  /**
   * Base attributes of this commodity.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be {@code containerType.featuresType == commodityInfo.commodityType.containerFeaturesType}.<br/>
   */
  CommodityInfo commodityInfo

  /**
   * The maximally allowed weight per container for this commodity (usually dictated by some policy).
   * <p/>
   * Note that this weight is always lesser than or equal to the maximally allowed weight of corresponding {@code containerType}. It can be lesser if some policy dictates that we should not reach
   * the absolute maximum of the {@code containerType}.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to one kilogram.<br/>
   * Must be less than or equal to {@code containerType.maxCommodityWeight}.<br/>
   * Must be greater than or equal to {@code maxRecommendedWeightPerContainer}.<br/>
   * Must have a kilogram units and a whole number value.<br/>
   */
  Quantity<Mass> maxAllowedWeightPerContainer

  /**
   * The maximum recommended weight per container.
   * <p/>
   * This value is calculated when we spread the total weight of a commodity across all containers. In other words, this value is the rounded up quotient of commodity total weight and the number of
   * containers.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to one kilogram.<br/>
   * Must be less than or equal to {@code maxAllowedWeightPerContainer}.<br/>
   * Must have a kilogram units and a whole number value.<br/>
   * Must be {@code maxRecommendedWeightPerContainer.value * containerCount >= commodityInfo.totalWeight.value}.<br/>
   */
  Quantity<Mass> maxRecommendedWeightPerContainer

  /**
   * The number of containers required to carry the total weight of a commodity.
   * <p/>
   * During calculation, maxAllowedWeightPerContainer is taken into account.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to {@code 1}.<br/>
   */
  Integer containerCount

  /**
   * Creates {@code Commodity} instance based on required properties and calculates derived properties.
   * <p/>
   * It is recommended to always use this factory method instead of map constructor because map constructor requires that all derived values are correctly precalculated.
   */
  static Commodity make(ContainerType containerType, CommodityInfo commodityInfo, Quantity<Mass> maxAllowedWeightPerContainer) {
    BigDecimal commodityTotalWeightValueInKilograms = commodityInfo.totalWeight.value
    Quantity<Mass> maxAllowedWeightPerContainerInKilograms = maxAllowedWeightPerContainer.to(Units.KILOGRAM)
    MathContext mathContext = new MathContext(7, RoundingMode.HALF_UP)

    Integer commodityContainerCount = commodityTotalWeightValueInKilograms
        .divide(maxAllowedWeightPerContainerInKilograms.value.toBigDecimal(), mathContext)
        .setScale(0, RoundingMode.UP)
        .toInteger()

    Integer maxRecommendedWeightPerContainerValueInKilograms = commodityTotalWeightValueInKilograms
        .divide(commodityContainerCount.toBigDecimal(), mathContext)
        .setScale(0, RoundingMode.UP)
        .toInteger()

    Quantity<Mass> maxRecommendedWeightPerContainerInKilograms = Quantities.getQuantity(maxRecommendedWeightPerContainerValueInKilograms, Units.KILOGRAM)

    Commodity commodity = new Commodity(
        containerType: containerType,
        commodityInfo: commodityInfo,
        maxAllowedWeightPerContainer: maxAllowedWeightPerContainerInKilograms,
        maxRecommendedWeightPerContainer: maxRecommendedWeightPerContainerInKilograms,
        containerCount: commodityContainerCount
    )

    return commodity
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(containerType, notNullValue())
    requireMatch(commodityInfo, notNullValue())
    requireMatch(maxAllowedWeightPerContainer, notNullValue())
    requireMatch(maxRecommendedWeightPerContainer, notNullValue())
    requireMatch(containerCount, notNullValue())

    requireTrue(containerType.featuresType == commodityInfo.commodityType.containerFeaturesType)

    requireTrue(Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(maxAllowedWeightPerContainer))
    requireTrue(((ComparableQuantity)containerType.maxCommodityWeight).isGreaterThanOrEqualTo(maxAllowedWeightPerContainer))

    requireTrue(Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(maxRecommendedWeightPerContainer))
    requireTrue(((ComparableQuantity)maxAllowedWeightPerContainer).isGreaterThanOrEqualTo(maxRecommendedWeightPerContainer))

    // Note: all weights have to be in kilograms and rounded (without decimal part).
    requireTrue(maxAllowedWeightPerContainer.unit == Units.KILOGRAM)
    requireTrue(maxAllowedWeightPerContainer.value.toBigDecimal().scale() == 0)

    requireTrue(maxRecommendedWeightPerContainer.unit == Units.KILOGRAM)
    requireTrue(maxRecommendedWeightPerContainer.value.toBigDecimal().scale() == 0)

    requireTrue(containerCount >= 1)

    requireTrue((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= (commodityInfo.totalWeight.value.toBigDecimal()))
  }
}
