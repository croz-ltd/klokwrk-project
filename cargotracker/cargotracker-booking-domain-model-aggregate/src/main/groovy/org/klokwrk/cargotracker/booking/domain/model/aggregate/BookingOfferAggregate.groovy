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
package org.klokwrk.cargotracker.booking.domain.model.aggregate

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.quantity.Mass
import java.math.MathContext
import java.math.RoundingMode

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class BookingOfferAggregate {
  BookingOfferId bookingOfferId
  RouteSpecification routeSpecification
  BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

  // TODO dmurat: extract this method into factory service
  private static Commodity calculateCommodity(ContainerDimensionType containerDimensionType, CommodityInfo commodityInfo) {
    ContainerType containerType = ContainerType.find(containerDimensionType, commodityInfo.commodityType.containerFeaturesType)

    // TODO dmurat: max allowed weight per container policy.
    //              Extract this logic in domain service behind AllowedCommodityWeightPerContainerPolicy.allowedWeight(ContainerType) interface
    Quantity<Mass> commodityMaxAllowedWeightPerContainerPerPolicyInKilograms = toQuantityPercent(95, containerType.maxCommodityWeight, Units.KILOGRAM, RoundingMode.DOWN)

    BigDecimal commodityTotalWeightValueInKilograms = commodityInfo.totalWeight.value

    MathContext mathContext = new MathContext(7, RoundingMode.HALF_UP)
    Integer commodityContainerCount = commodityTotalWeightValueInKilograms
        .divide(commodityMaxAllowedWeightPerContainerPerPolicyInKilograms.value.toBigDecimal(), mathContext)
        .setScale(0, RoundingMode.UP)
        .toInteger()

    // TODO dmurat: evaluate if we need this policy too
    // max container count per commodity type policy
    // very similar policy we have in BookingOfferCommodities.canAcceptCommodity(). But there it is cumulative across thw whole BookingOffer.
    // will comment for now, and rely on cumulative policy only. Maybe introduce later
//    if (commodityContainerCount > 5000) {
//      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("bookingOfferAggregate.commodityContainerCountTooHigh"))
//    }

    Integer commodityMaxRecommendedWeightPerContainerValueInKilograms = commodityTotalWeightValueInKilograms
        .divide(commodityContainerCount.toBigDecimal(), mathContext)
        .setScale(0, RoundingMode.UP)
        .toInteger()

    Quantity<Mass> commodityMaxRecommendedWeightPerContainer = Quantities.getQuantity(commodityMaxRecommendedWeightPerContainerValueInKilograms, Units.KILOGRAM)

    Commodity commodity = new Commodity(
        containerType: ContainerType.find(containerDimensionType, commodityInfo.commodityType.containerFeaturesType),
        commodityInfo: commodityInfo,
        maxAllowedWeightPerContainer: commodityMaxAllowedWeightPerContainerPerPolicyInKilograms,
        maxRecommendedWeightPerContainer: commodityMaxRecommendedWeightPerContainer,
        containerCount: commodityContainerCount
    )

    return commodity
  }

  // to be extracted in policy (domain service)
  private static <T extends Quantity<T>> Quantity<T> toQuantityPercent(Integer percent, Quantity<T> quantity, Unit<T> targetUnit = null, RoundingMode roundingMode = RoundingMode.HALF_UP) {
    if (percent == null) {
      return null
    }

    if (quantity == null) {
      return null
    }

    Unit<T> targetUnitToUse = targetUnit
    if (targetUnit == null) {
      targetUnitToUse = quantity.unit
    }

    Quantity<T> quantityInTargetUnit = quantity.to(targetUnitToUse)
    BigDecimal percentValueRounded = ((quantityInTargetUnit.value * percent / 100) as BigDecimal).setScale(0, roundingMode)

    Quantity<T> quantity90PercentRounded = Quantities.getQuantity(percentValueRounded.toBigInteger(), targetUnitToUse)
    return quantity90PercentRounded
  }

  @AggregateIdentifier
  String getAggregateIdentifier() {
    // Note: Must use null safe navigation here as cargoId might be null when first command is not successful (and axon requires aggregate identifier for further processing)
    return bookingOfferId?.identifier
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.ALWAYS)
  BookingOfferAggregate createBookingOffer(CreateBookingOfferCommand createBookingOfferCommand, MetaData metaData) {
    Commodity commodity = calculateCommodity(createBookingOfferCommand.containerDimensionType, createBookingOfferCommand.commodityInfo)

    // Check for container count per commodity type.
    // The largest ship in the world can carry 24000 containers. We should limit container count to the max of 5000 per a single booking, for example.
    // We can have two different policies here. One for limiting container count per commodity type, and another one for limiting container count for booking.
    // In a simpler case, both policies can be the same. In that case with a single commodity type we can allocate complete booking capacity.
    if (!bookingOfferCommodities.canAcceptCommodity(commodity)) { // TODO dmurat: container count per booking policy
      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey("bookingOfferAggregate.bookingOfferCommodities.cannotAcceptCommodity"))
    }

    // Note: cannot store here directly as state change should happen in event sourcing handler.
    //       Alternative is to publish two events (second one applied after the first one updates the state), but we do not want that.
    Tuple2<Quantity<Mass>, Integer> preCalculatedTotals = bookingOfferCommodities.preCalculateTotals(commodity)
    Quantity<Mass> bookingTotalCommodityWeight = preCalculatedTotals.v1
    Integer bookingTotalContainerCount = preCalculatedTotals.v2

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        bookingOfferId: createBookingOfferCommand.bookingOfferId,
        routeSpecification: createBookingOfferCommand.routeSpecification,
        commodity: commodity,
        bookingTotalCommodityWeight: bookingTotalCommodityWeight,
        bookingTotalContainerCount: bookingTotalContainerCount
    )

    apply(bookingOfferCreatedEvent, metaData)
    return this
  }

  @EventSourcingHandler
  void onBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent) {
    bookingOfferId = bookingOfferCreatedEvent.bookingOfferId
    routeSpecification = bookingOfferCreatedEvent.routeSpecification
    bookingOfferCommodities.storeCommodity(bookingOfferCreatedEvent.commodity)
  }
}
