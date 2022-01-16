package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Default implementation of {@link CommodityCreatorService}.
 * <p/>
 * For determining maximum allowed weight per container, it uses provided {@link MaxAllowedWeightPerContainerPolicy} service instance.
 */
@CompileStatic
class DefaultCommodityCreatorService implements CommodityCreatorService {
  MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy

  /**
   * Constructor.
   * <p/>
   * Provided {@link MaxAllowedWeightPerContainerPolicy} instance must not be {@code null}.
   */
  DefaultCommodityCreatorService(MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy) {
    requireMatch(maxAllowedWeightPerContainerPolicy, notNullValue())

    this.maxAllowedWeightPerContainerPolicy = maxAllowedWeightPerContainerPolicy
  }

  @Override
  Commodity from(ContainerDimensionType containerDimensionType, CommodityInfo commodityInfo) {
    ContainerType containerType = ContainerType.find(containerDimensionType, commodityInfo.commodityType.containerFeaturesType)
    Quantity<Mass> commodityMaxAllowedWeightPerContainerPerPolicyInKilograms = maxAllowedWeightPerContainerPolicy.maxAllowedWeightPerContainer(containerType)
    Commodity commodity = Commodity.make(containerType, commodityInfo, commodityMaxAllowedWeightPerContainerPerPolicyInKilograms)

    // Potentially we might also want the max container TEU count per commodity type policy
    // This is very similar policy we have in BookingOfferCommodities.canAcceptCommodity(). But there it is cumulative across thw whole BookingOffer.
    // We will not implement this at the moment, and we'll rely on cumulative policy only. Maybe we can introduce this later.
//    if (commodityContainerTeuCount > 5000) {
//      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("bookingOfferAggregate.commodityContainerTeuCountTooHigh"))
//    }

    return commodity
  }
}
