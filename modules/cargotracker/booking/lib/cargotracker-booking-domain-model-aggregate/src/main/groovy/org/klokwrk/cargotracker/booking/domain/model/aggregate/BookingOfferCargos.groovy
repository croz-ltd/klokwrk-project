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
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass

/**
 * Handles cargos at the {@link BookingOfferAggregate} level.
 * <p/>
 * Encapsulates invariant checks and access to the internal {@link CommodityType} to {@link Cargo} map. Therefore, at the {@code BookingOfferAggregate} level, we can have only a single
 * {@link Cargo} for each {@link CommodityType}.
 */
@CompileStatic
class BookingOfferCargos {
  private final Map<CommodityType, Cargo> commodityTypeToCargoMap = [:]

  private Quantity<Mass> totalCommodityWeight = Quantities.getQuantity(0, Units.KILOGRAM)
  private BigDecimal totalContainerTeuCount = 0 // should be constrained to the max of, say 5000

  Map<CommodityType, Cargo> getCommodityTypeToCargoMap() {
    return Collections.unmodifiableMap(commodityTypeToCargoMap)
  }

  Quantity<Mass> getTotalCommodityWeight() {
    return totalCommodityWeight
  }

  BigDecimal getTotalContainerTeuCount() {
    return totalContainerTeuCount
  }

  /**
   * Checks if we can accept the {@link Cargo} at the {@link BookingOfferAggregate} level.
   * <p/>
   * We should use this method from the aggregate's command handler to check if it is valid to add the {@code Cargo} instance to the aggregate state. Actual state change happens later in the
   * event sourcing handler. Note that we cannot make this check in the event sourcing handler because it must make changes unconditionally to support rehydration from past events.
   */
  boolean canAcceptCargo(Cargo cargo, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    BigDecimal newTotalContainerTeuCount = totalContainerTeuCount + cargo.containerTeuCount
    return maxAllowedTeuCountPolicy.isTeuCountAllowed(newTotalContainerTeuCount)
  }

  /**
   * Without changing state of {@code BookingOfferCargos} instance, calculates totals in the same way as they will be calculated once the cargo is stored via {@link #storeCargo(Cargo)}.
   * <p/>
   * This pre-calculation is used from the aggregate's command handler to calculate the totals required to create an event. The alternative would be to publish two events where the second one is
   * created based on state changes caused by the first event. However, we need pre-calculation as we want to publish a single event.
   * <p/>
   * This method is very similar to the {@link #calculateNewTotals(Cargo)}, but this one also checks if we can accept the cargo.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> preCalculateTotals(Cargo cargo, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    if (!canAcceptCargo(cargo, maxAllowedTeuCountPolicy)) {
      throw new AssertionError("Cannot proceed with calculating totals since cargo is not acceptable." as Object)
    }

    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateNewTotals(cargo)
    return totalsTuple
  }

  // use this one in eventSourcingHandler to store past events unconditionally, regardless of potential change in previous business logic
  /**
   * Stores the cargo in the internal map by replacing any cargo previously stored under the same {@link CommodityType} key.
   * <p/>
   * We should use this method only from the event sourcing handler as it changes the aggregate state and does it unconditionally without checking any invariants. This is necessary to support correct
   * rehydration of the aggregate from previous events. We should do all invariant checking in the aggregate's command handler.
   */
  void storeCargo(Cargo cargo) {
    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateNewTotals(cargo)

    totalCommodityWeight = totalsTuple.v1
    totalContainerTeuCount = totalsTuple.v2

    commodityTypeToCargoMap.put(cargo.commodity.commodityType, cargo)
  }

  /**
   * Without changing the aggregate state, calculates new totals based on provided {@link Cargo} and the current aggregate state.
   * <p/>
   * This method is very similar to the {@link #preCalculateTotals(Cargo, MaxAllowedTeuCountPolicy)}, but this one does not check if the cargo can be accepted or not.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> calculateNewTotals(Cargo cargo) {
    Quantity<Mass> newTotalCommodityWeight
    BigDecimal newTotalContainerTeuCount

    Cargo cargoOld = commodityTypeToCargoMap.get(cargo.commodity.commodityType)
    if (cargoOld == null) {
      newTotalCommodityWeight = totalCommodityWeight.add(cargo.commodity.weight)
      newTotalContainerTeuCount = totalContainerTeuCount + cargo.containerTeuCount
    }
    else {
      newTotalCommodityWeight = totalCommodityWeight.subtract(cargoOld.commodity.weight).add(cargo.commodity.weight)
      newTotalContainerTeuCount = totalContainerTeuCount - cargoOld.containerTeuCount + cargo.containerTeuCount
    }

    return new Tuple2<Quantity<Mass>, BigDecimal>(newTotalCommodityWeight, newTotalContainerTeuCount)
  }
}
