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
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.math.RoundingMode

import static org.hamcrest.Matchers.notNullValue

/**
 * Models a containerized cargo consisting of related commodity and container attributes, together with cargo attributes derived from contained commodity and container characteristics.
 * <p/>
 * Majority of cargo attributes are calculated from {@link ContainerDimensionType} and {@link Commodity}, as demonstrated in {@code BookingOfferAggregate}.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class Cargo implements PostMapConstructorCheckable {
  /**
   * Container type associated with this cargo.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be {@code containerType.featuresType == commodity.commodityType.containerFeaturesType}.<br/>
   */
  ContainerType containerType

  /**
   * Commodity of this cargo.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be {@code containerType.featuresType == commodity.commodityType.containerFeaturesType}.<br/>
   */
  Commodity commodity

  /**
   * The maximally allowed commodity weight per container for this cargo (usually dictated by some policy).
   * <p/>
   * Note that this weight is always lesser than or equal to the maximally allowed commodity weight of corresponding {@code containerType}. It can be lesser if some policy dictates that we should not
   * reach the absolute maximum of the commodity weight for particular {@code containerType}.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to one kilogram.<br/>
   * Must be less than or equal to {@code containerType.maxCommodityWeight}.<br/>
   * Must be greater than or equal to {@code maxRecommendedWeightPerContainer}.<br/>
   * Must have a kilogram units with a whole number value.<br/>
   */
  Quantity<Mass> maxAllowedWeightPerContainer

  /**
   * Derived property representing the maximum recommended commodity weight per container.
   * <p/>
   * This value is calculated when we spread the weight of a commodity across all containers. In other words, this value is the rounded up quotient of commodity weight and the number of containers.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to one kilogram.<br/>
   * Must be less than or equal to {@code maxAllowedWeightPerContainer}.<br/>
   * Must have a kilogram units and a whole number value.<br/>
   * Must be {@code maxRecommendedWeightPerContainer.value * containerCount >= commodity.weight.value}.<br/>
   */
  Quantity<Mass> maxRecommendedWeightPerContainer

  /**
   * Derived property representing the number of containers required to carry the weight of a related commodity.
   * <p/>
   * During calculation, maxAllowedWeightPerContainer is taken into account.
   * <p/>
   * Note that, in shipping, containerCount is just informal information. On the other side, TEU count is much more valuable as standard measurement for container quantity.
   * <p/>
   * Must not be {@code null}.<br/>
   * Must be greater than or equal to {@code 1}.<br/>
   */
  Integer containerCount

  /**
   * Derived property representing the number of containers expressed as Twenty-foot Equivalent Units (TEU).
   * <p/>
   * Must not be {@code null}.<br/>
   * Must have a {@code scale} between 0 and 2 inclusive.<br/>
   * Must be equal to {@code containerCount * containerType.dimensionType.teu} rounded up to two decimals.<br/>
   */
  BigDecimal containerTeuCount

  /**
   * Creates a {@code Cargo} instance based on required properties and calculates derived properties.
   * <p/>
   * It is recommended to always use this factory method instead of map constructor because map constructor requires that all derived values are correctly precalculated.
   * <p/>
   * When {@code maxAllowedWeightPerContainer} parameter is null, {@code maxAllowedWeightPerContainer} is equal to the {@code containerType.maxCommodityWeight}.
   */
  @SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
  static Cargo make(ContainerType containerType, Commodity commodity, Quantity<Mass> maxAllowedWeightPerContainer = null) {
    BigDecimal weightValueKg = commodity.weight.to(Units.KILOGRAM).value
    Quantity<Mass> maxAllowedWeightPerContainerKg = maxAllowedWeightPerContainer?.to(Units.KILOGRAM)
    if (maxAllowedWeightPerContainerKg == null) {
      maxAllowedWeightPerContainerKg = containerType.maxCommodityWeight.to(Units.KILOGRAM)
    }

    Integer containerCount = weightValueKg
        .divide(maxAllowedWeightPerContainerKg.value.toBigDecimal(), 0, RoundingMode.UP)
        .toInteger()

    Integer maxRecommendedWeightPerContainerValueKg = weightValueKg
        .divide(containerCount.toBigDecimal(), 0, RoundingMode.UP)
        .toInteger()

    Quantity<Mass> maxRecommendedWeightPerContainerKg = maxRecommendedWeightPerContainerValueKg.kg

    BigDecimal containerTeuCount = (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP)

    Cargo cargo = new Cargo(
        containerType: containerType,
        commodity: commodity,
        maxAllowedWeightPerContainer: maxAllowedWeightPerContainerKg,
        maxRecommendedWeightPerContainer: maxRecommendedWeightPerContainerKg,
        containerCount: containerCount,
        containerTeuCount: containerTeuCount
    )

    return cargo
  }

  @SuppressWarnings(["CodeNarc.AbcMetric", "CodeNarc.DuplicateNumberLiteral"])
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(containerType, notNullValue())
    requireMatch(this.commodity, notNullValue())
    requireMatch(maxAllowedWeightPerContainer, notNullValue())
    requireMatch(maxRecommendedWeightPerContainer, notNullValue())
    requireMatch(containerCount, notNullValue())
    requireMatch(containerTeuCount, notNullValue())

    requireTrue(containerType.featuresType == commodity.commodityType.containerFeaturesType)

    requireTrue(1.kg.isLessThanOrEqualTo(maxAllowedWeightPerContainer))
    requireTrue(containerType.maxCommodityWeight.toComparable().isGreaterThanOrEqualTo(maxAllowedWeightPerContainer))

    requireTrue(1.kg.isLessThanOrEqualTo(maxRecommendedWeightPerContainer))
    requireTrue(maxAllowedWeightPerContainer.toComparable().isGreaterThanOrEqualTo(maxRecommendedWeightPerContainer))

    // Note: all weights have to be in kilograms and rounded (without decimal part).
    requireTrue(maxAllowedWeightPerContainer.unit == Units.KILOGRAM)
    requireTrue(maxAllowedWeightPerContainer.value.toBigDecimal().scale() == 0)

    requireTrue(maxRecommendedWeightPerContainer.unit == Units.KILOGRAM)
    requireTrue(maxRecommendedWeightPerContainer.value.toBigDecimal().scale() == 0)

    requireTrue(containerCount >= 1)
    requireTrue(containerTeuCount.scale() <= 2)
    requireTrue(containerTeuCount.scale() >= 0)

    requireTrue(containerTeuCount == (containerCount * containerType.dimensionType.teu).setScale(2, RoundingMode.UP))

    requireTrue((maxRecommendedWeightPerContainer.value.toBigDecimal() * containerCount) >= (commodity.weight.value.toBigDecimal()))
  }
}
