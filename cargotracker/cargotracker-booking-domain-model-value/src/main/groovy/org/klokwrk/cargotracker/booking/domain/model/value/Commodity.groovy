package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Encapsulates all attributes for a commodity, including the assigned container type.
 * <p/>
 * All attributes can be calculated from {@link ContainerDimensionType} and {@link CommodityInfo}, as demonstrated in {@code CargoAggregate}.
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
