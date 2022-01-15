package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.quantity.Mass
import java.math.RoundingMode

import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.lessThanOrEqualTo
import static org.hamcrest.Matchers.notNullValue

/**
 * {@link MaxAllowedWeightPerContainerPolicy} implementation that calculates the allowed weight based on percentage of absolute maximum weight allowed by container type.
 * <p/>
 * For example, if {@link ContainerType} allows for maximum weight of {@code 25_000} kilograms, this policy implementation for {@code 90%} will reduce it further to {@code 22_500} kilograms.
 */
@CompileStatic
class PercentBasedMaxAllowedWeightPerContainerPolicy implements MaxAllowedWeightPerContainerPolicy {
  Integer percentOfContainerTypeMaxCommodityWeight
  Unit<Mass> targetUnitOfMass
  RoundingMode roundingMode

  /**
   * Constructor.
   * <p/>
   * Default params values:
   * <ul>
   * <li>{@code targetUnitOfMass}: {@code Units.KILOGRAM}</li>
   * <li>{@code roundingMode}: {@code RoundingMode.DOWN}</li>
   * </ul>
   * <p/>
   * All parameters must not be {@code null}.<br/>
   * Parameter {@code percentOfContainerTypeMaxCommodityWeight} must be between {@code 1} and {@code 100} inclusive.
   */
  PercentBasedMaxAllowedWeightPerContainerPolicy(Integer percentOfContainerTypeMaxCommodityWeight, Unit<Mass> targetUnitOfMass = Units.KILOGRAM, RoundingMode roundingMode = RoundingMode.DOWN) {
    requireMatch(percentOfContainerTypeMaxCommodityWeight, notNullValue())
    requireMatch(targetUnitOfMass, notNullValue())
    requireMatch(roundingMode, notNullValue())

    requireMatch(percentOfContainerTypeMaxCommodityWeight, greaterThanOrEqualTo(1))
    requireMatch(percentOfContainerTypeMaxCommodityWeight, lessThanOrEqualTo(100))

    this.percentOfContainerTypeMaxCommodityWeight = percentOfContainerTypeMaxCommodityWeight
    this.targetUnitOfMass = targetUnitOfMass
    this.roundingMode = roundingMode
  }

  @SuppressWarnings("CodeNarc.DuplicateNumberLiteral")
  @Override
  Quantity<Mass> maxAllowedWeightPerContainer(ContainerType containerType) {
    Quantity<Mass> quantityInTargetUnit = containerType.maxCommodityWeight.to(targetUnitOfMass)
    BigDecimal percentValueRounded = ((quantityInTargetUnit.value * percentOfContainerTypeMaxCommodityWeight / 100) as BigDecimal).setScale(0, roundingMode)

    Quantity<Mass> roundedPercentOfQuantity = Quantities.getQuantity(percentValueRounded.toBigInteger(), targetUnitOfMass)
    return roundedPercentOfQuantity
  }
}
