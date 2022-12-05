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
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

/**
 * Handles cargos at the {@link BookingOfferAggregate} level.
 * <p/>
 * Encapsulates invariant checks and access to the internal {@code bookingOfferCargoMap} map. The {@code bookingOfferCargoMap} key is a string representation of {@code BookingOfferCargoMapKey}, which
 * encapsulates distinguishing properties of cargo at the level of booking offer. In other words, {@code BookingOfferCargoMapKey} properties determine which cargos are considered equal in the context
 * of a booking offer.
 * <p/>
 * When multiple equivalent booking offer cargos are added, {@code BookingOfferCargos} instance maintains only a single cargo instance representing all of them. Consequently, some cargo attributes
 * must be consolidated when cargos are added or removed from a booking offer.
 * <p/>
 * For example, if we add multiple cargos with the same commodity type, container type, and requested storage temperature, {@code BookingOfferCargos} will summarize all their weights inside a single
 * cargo instance with the same commodity type, container type, and requested storage temperature.
 * <p/>
 * In the context of a booking offer, we are not interested in multiple submissions of the equivalent cargos that are different only in their weight. Rather, we are consolidating all such submissions
 * with a single record of equivalent cargo with correctly summed up container weight.
 */
@CompileStatic
class BookingOfferCargos {
  /**
   * Encapsulates distinguishing properties of a cargo at the level of booking offer.
   */
  @KwrkImmutable(knownImmutableClasses = [Quantity])
  static class BookingOfferCargoMapKey {
    CommodityType commodityType
    ContainerType containerType
    Quantity<Temperature> commodityRequestedStorageTemperature

    static BookingOfferCargoMapKey fromCargo(Cargo cargo) {
      BookingOfferCargoMapKey bookingOfferCargoMapKey = new BookingOfferCargoMapKey(
          commodityType: cargo.commodity.commodityType,
          containerType: cargo.containerType,
          commodityRequestedStorageTemperature: cargo.commodity.requestedStorageTemperature
      )

      return bookingOfferCargoMapKey
    }

    static String fromCargoAsString(Cargo cargo) {
      BookingOfferCargoMapKey bookingOfferCargoMapKey = fromCargo(cargo)
      String bookingOfferCargoMapKeyAsString = bookingOfferCargoMapKey.toStringKey()
      return bookingOfferCargoMapKeyAsString
    }

    String toStringKey() {
      String commodityRequestedStorageTemperatureString = commodityRequestedStorageTemperature == null ? null : QuantityFormatter.instance.format(commodityRequestedStorageTemperature)
      return "${ commodityType.name() }:::${ containerType.name() }:::${ commodityRequestedStorageTemperatureString }".toString()
    }
  }

  private final Map<String, Cargo> bookingOfferCargoMap = [:]

  private Quantity<Mass> totalCommodityWeight = Quantities.getQuantity(0, Units.KILOGRAM)
  private BigDecimal totalContainerTeuCount = 0 // should be constrained to the max of, say 5000

  Map<String, Cargo> getBookingOfferCargoMap() {
    return Collections.unmodifiableMap(bookingOfferCargoMap)
  }

  Quantity<Mass> getTotalCommodityWeight() {
    return totalCommodityWeight
  }

  BigDecimal getTotalContainerTeuCount() {
    return totalContainerTeuCount
  }

  /**
   * Checks if we can accept addition of a {@link Cargo} at the {@link BookingOfferAggregate} level.
   * <p/>
   * We should use this method from the aggregate's command handler to check if it is valid to add the {@code Cargo} instance to the aggregate state. Actual state change happens later in the
   * event sourcing handler. Note that we cannot make this check in the event sourcing handler because it must make changes unconditionally to support rehydration from past events.
   */
  boolean canAcceptCargoAddition(Cargo cargo, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    BigDecimal newTotalContainerTeuCount = totalContainerTeuCount + cargo.containerTeuCount
    return maxAllowedTeuCountPolicy.isTeuCountAllowed(newTotalContainerTeuCount)
  }

  /**
   * Without changing state of {@code BookingOfferCargos} instance, calculates totals in the same way as they will be calculated once the cargo is stored via {@link #storeCargoAddition(Cargo)}.
   * <p/>
   * This pre-calculation is used from the aggregate's command handler to calculate the totals required to create an event. The alternative would be to publish two events where the second one is
   * created based on state changes caused by the first event. However, we need pre-calculation as we want to publish a single event.
   * <p/>
   * This method is very similar to the {@link #calculateTotalsForCargoAddition(Cargo)}, but this one also checks if we can accept the cargo addition.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> preCalculateTotalsForCargoAddition(Cargo cargo, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    if (!canAcceptCargoAddition(cargo, maxAllowedTeuCountPolicy)) {
      throw new AssertionError("Cannot proceed with calculating totals since cargo is not acceptable." as Object)
    }

    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateTotalsForCargoAddition(cargo)
    return totalsTuple
  }

  // use this one in eventSourcingHandler to store past events unconditionally, regardless of potential change in previous business logic
  /**
   * Stores the cargo addition in the internal map by replacing any previously stored equivalent cargo.
   * <p/>
   * We should use this method only from the event sourcing handler as it changes the aggregate state and does it unconditionally without checking any invariants. This is necessary to support correct
   * rehydration of the aggregate from previous events. We should do all invariant checking in the aggregate's command handler.
   */
  void storeCargoAddition(Cargo cargo) {
    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateTotalsForCargoAddition(cargo)
    totalCommodityWeight = totalsTuple.v1
    totalContainerTeuCount = totalsTuple.v2

    Cargo existingCargo = bookingOfferCargoMap.get(BookingOfferCargoMapKey.fromCargoAsString(cargo))
    if (existingCargo == null) {
      bookingOfferCargoMap.put(BookingOfferCargoMapKey.fromCargoAsString(cargo), cargo)
    }
    else {
      Commodity existingCommodity = existingCargo.commodity
      Commodity newCommodityWithAddedWeight = Commodity.make(existingCommodity.commodityType, existingCommodity.weight.add(cargo.commodity.weight), existingCommodity.requestedStorageTemperature)
      Cargo newCargoWithAddedCommodityWeight = Cargo.make(existingCargo.containerType, newCommodityWithAddedWeight, existingCargo.maxAllowedWeightPerContainer)
      bookingOfferCargoMap.put(BookingOfferCargoMapKey.fromCargoAsString(existingCargo), newCargoWithAddedCommodityWeight)
    }
  }

  /**
   * Without changing the aggregate state, calculates new totals for cargo addition based on provided {@link Cargo} and the current aggregate state.
   * <p/>
   * This method is very similar to the {@link #preCalculateTotalsForCargoAddition(Cargo, MaxAllowedTeuCountPolicy)}, but this one does not check if the cargo can be accepted or not.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> calculateTotalsForCargoAddition(Cargo cargo) {
    Quantity<Mass> newTotalCommodityWeight
    BigDecimal newTotalContainerTeuCount

    Cargo existingCargo = bookingOfferCargoMap.get(BookingOfferCargoMapKey.fromCargoAsString(cargo))
    if (existingCargo == null) {
      newTotalCommodityWeight = totalCommodityWeight.add(cargo.commodity.weight)
      newTotalContainerTeuCount = totalContainerTeuCount + cargo.containerTeuCount
    }
    else {
      Commodity existingCommodity = existingCargo.commodity

      // throwaway instances, used only for new teuCount calculation
      Commodity newCommodityWithAddedWeight = Commodity.make(existingCommodity.commodityType, existingCommodity.weight.add(cargo.commodity.weight), existingCommodity.requestedStorageTemperature)
      Cargo newCargoWithAddedCommodityWeight = Cargo.make(existingCargo.containerType, newCommodityWithAddedWeight, existingCargo.maxAllowedWeightPerContainer)

      newTotalCommodityWeight = totalCommodityWeight.subtract(existingCargo.commodity.weight).add(newCargoWithAddedCommodityWeight.commodity.weight)
      newTotalContainerTeuCount = totalContainerTeuCount - existingCargo.containerTeuCount + newCargoWithAddedCommodityWeight.containerTeuCount
    }

    return new Tuple2<Quantity<Mass>, BigDecimal>(newTotalCommodityWeight, newTotalContainerTeuCount)
  }
}
