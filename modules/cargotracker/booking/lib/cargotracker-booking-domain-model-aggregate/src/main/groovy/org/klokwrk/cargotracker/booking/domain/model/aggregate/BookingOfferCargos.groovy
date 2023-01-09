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
package org.klokwrk.cargotracker.booking.domain.model.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.hamcrest.Matchers.notNullValue

/**
 * Handles cargos at the {@link BookingOfferAggregate} level, by encapsulating some invariant checks and keeping the internal {@code bookingOfferCargoCollection} consolidated.
 * <p/>
 * Equality of cargos in the context of a booking offer is defined by {@code BookingOfferCargoEquality}. Internal {@code bookingOfferCargoCollection} is consolidated when it contains only a single
 * {@code Cargo} instance for corresponding {@code BookingOfferCargoEquality}. When adding or removing {@code Cargo} instances, internal {@code bookingOfferCargoCollection} has to be kept
 * consolidated.
 * <p/>
 * For example, if we add multiple cargos with the same commodity type, container type, and requested storage temperature (those properties together define booking offer cargo equality),
 * {@code BookingOfferCargos} will summarize all their weights inside a new booking offer cargo instance (this is a consolidated cargo instance) with the same commodity type, container type, and
 * requested storage temperature. In the process, consolidated cargo will also contain correctly updated {@code maxRecommendedWeightPerContainerKg}, {@code containerCount}, and
 * {@code containerTeuCount} properties.
 * <p/>
 * The new consolidated cargo instance will take {@code maxAllowedWeightPerContainer} of equivalent cargo instances that are just added (or removed) to the {@code BookingOfferCargos}. This means that
 * old {@code maxAllowedWeightPerContainer} is dropped and forgotten.
 * <p/>
 * Why are we using cargo consolidation at all? In the context of a booking offer, we are not interested in multiple submissions of the equivalent cargos that are different only in their weight.
 * Instead, we are consolidating all such requests with a single record of equivalent cargo with correctly summed up container weight and other derived properties like
 * {@code maxRecommendedWeightPerContainerKg}, {@code containerCount}, and {@code containerTeuCount}.
 */
@CompileStatic
class BookingOfferCargos {

  @SuppressWarnings("DuplicatedCode")
  static Collection<Cargo> consolidateCargoCollectionsForCargoAddition(Collection<Cargo> consolidatedCargoCollectionStartingPoint, Collection<Cargo> cargoCollectionToAdd) {
    if (!cargoCollectionToAdd) {
      return consolidatedCargoCollectionStartingPoint
    }

    Collection<Cargo> myConsolidatedCargoCollectionStartingPoint = consolidatedCargoCollectionStartingPoint ?: [] as Collection<Cargo>
    Map<BookingOfferCargoEquality, Cargo> allCargoConsolidatedMap = [:]

    // Note: Besides determining resultant consolidated map, we are also using cargoListToAddMap as a base for determining maxAllowedWeightPerContainer to use
    Map<BookingOfferCargoEquality, List<Cargo>> cargoListToAddMap = cargoCollectionToAdd.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })
    Map<BookingOfferCargoEquality, List<Cargo>> consolidatedCargoListStartingPointMap = myConsolidatedCargoCollectionStartingPoint.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })

    // First, add new entries that have existing equivalents in starting point
    cargoListToAddMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityToAdd ->
      Collection<Quantity<Mass>> cargoCommodityWeightQuantities = cargoListToAddMap.get(bookingOfferCargoEqualityToAdd)*.commodity.weight
      if (consolidatedCargoListStartingPointMap.get(bookingOfferCargoEqualityToAdd) != null) {
        cargoCommodityWeightQuantities.addAll(consolidatedCargoListStartingPointMap.get(bookingOfferCargoEqualityToAdd)*.commodity.weight as Collection<Quantity<Mass>>)
      }

      Quantity<Mass> totalCargoCommodityWeightQuantity = 0.kg
      cargoCommodityWeightQuantities.each { Quantity<Mass> cargoCommodityWeightQuantity -> totalCargoCommodityWeightQuantity = totalCargoCommodityWeightQuantity + cargoCommodityWeightQuantity }

      // Note: selecting the cargoConsolidationBase element is significant because it determines maxAllowedWeightPerContainer to use with consolidated cargo.
      //       The maxAllowedWeightPerContainer of cargoConsolidationBase is determined by currently active MaxAllowedWeightPerContainerPolicy (during initial creation of a cargo value object in the
      //       aggregate).
      Cargo cargoConsolidationBase = cargoListToAddMap.get(bookingOfferCargoEqualityToAdd).first()

      Commodity consolidatedCommodity = Commodity.make(cargoConsolidationBase.commodity.commodityType, totalCargoCommodityWeightQuantity, cargoConsolidationBase.commodity.requestedStorageTemperature)
      Cargo consolidatedCargo = Cargo.make(cargoConsolidationBase.containerType, consolidatedCommodity, cargoConsolidationBase.maxAllowedWeightPerContainer)
      allCargoConsolidatedMap.put(bookingOfferCargoEqualityToAdd, consolidatedCargo)
    })

    // Second, add starting point entries that are not changed because there are no equivalents to add
    consolidatedCargoListStartingPointMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityStartingPoint ->
      if (!allCargoConsolidatedMap.containsKey(bookingOfferCargoEqualityStartingPoint)) {
        // Note: for not-changed cargos, we are not updating maxAllowedWeightPerContainer based on current MaxAllowedWeightPerContainerPolicy. If this is ok or not is a business decision.
        //       From the logical standpoint, it looks like maxAllowedWeightPerContainer of not-changed cargos should be updated also, but this would require adding MaxAllowedWeightPerContainerPolicy
        //       as additional method parameter. Therefore, we are not doing this at the moment, since it is not clear whether we should do this or not. If MaxAllowedWeightPerContainerPolicy does not
        //       change often and does not change dramatically, the influence of this decision should be minimal.
        allCargoConsolidatedMap.put(bookingOfferCargoEqualityStartingPoint, consolidatedCargoListStartingPointMap.get(bookingOfferCargoEqualityStartingPoint).first())
      }
    })

    Collection<Cargo> consolidatedCargoCollection = allCargoConsolidatedMap.values()
    return consolidatedCargoCollection
  }

  // TODO dmurat: commented code - will need this when support for cargo removal is added. Leaving it commented for now.
//  @SuppressWarnings("DuplicatedCode")
//  static Collection<Cargo> consolidateCargoCollectionsForCargoRemoval(Collection<Cargo> consolidatedCargoCollectionStartingPoint, Collection<Cargo> cargoCollectionToRemove) {
//    if (!cargoCollectionToRemove) {
//      return consolidatedCargoCollectionStartingPoint
//    }
//
//    Collection<Cargo> myConsolidatedCargoCollectionStartingPoint = consolidatedCargoCollectionStartingPoint ?: [] as Collection<Cargo>
//    Map<BookingOfferCargoEquality, Cargo> allCargoConsolidatedMap = [:]
//
//    // Note: Besides determining resultant consolidated map, we are also using cargoListToAddMap as a base for determining maxAllowedWeightPerContainer to use
//    Map<BookingOfferCargoEquality, List<Cargo>> cargoListToRemoveMap = cargoCollectionToRemove.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })
//    Map<BookingOfferCargoEquality, List<Cargo>> consolidatedCargoListStartingPointMap = myConsolidatedCargoCollectionStartingPoint.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })
//
//    // First, add new entries that have existing equivalents in starting point
//    cargoListToRemoveMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityToRemove ->
//      Collection<Quantity<Mass>> cargoCommodityWeightQuantitiesToRemove = cargoListToRemoveMap.get(bookingOfferCargoEqualityToRemove)*.commodity.weight
//      Quantity<Mass> cargoCommodityWeightQuantityStartingPoint = consolidatedCargoListStartingPointMap.get(bookingOfferCargoEqualityToRemove).first().commodity.weight
//
//      Quantity<Mass> totalCargoCommodityWeightQuantity = cargoCommodityWeightQuantityStartingPoint
//      cargoCommodityWeightQuantitiesToRemove.each({ Quantity<Mass> cargoCommodityWeightQuantityToRemove ->
//        totalCargoCommodityWeightQuantity = totalCargoCommodityWeightQuantity.subtract(cargoCommodityWeightQuantityToRemove)
//      })
//
//      if (totalCargoCommodityWeightQuantity.to(Units.KILOGRAM).value >= 1) { // Do not add (in effect, remove) cargos ending up with commodity weight lesser than 1 kg
//        // Note: selecting the cargoConsolidationBase element is very significant because it determines maxAllowedWeightPerContainer to use.
//        //       Here, cargo that we are removing determine the actual maxAllowedWeightPerContainer value that will be used for all equivalent cargos
//        Cargo cargoConsolidationBase = cargoListToRemoveMap.get(bookingOfferCargoEqualityToRemove).first()
//
//        Commodity consolidatedCommodity = Commodity.make(cargoConsolidationBase.commodity.commodityType, totalCargoCommodityWeightQuantity, cargoConsolidationBase.commodity.requestedStorageTemperature)
//        Cargo consolidatedCargo = Cargo.make(cargoConsolidationBase.containerType, consolidatedCommodity, cargoConsolidationBase.maxAllowedWeightPerContainer)
//        allCargoConsolidatedMap.put(bookingOfferCargoEqualityToRemove, consolidatedCargo)
//      }
//    })
//
//    // Second, add starting point entries that are not changed because there are no equivalents to remove
//    consolidatedCargoListStartingPointMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityStartingPoint ->
//      if (!cargoListToRemoveMap.containsKey(bookingOfferCargoEqualityStartingPoint)) {
//        allCargoConsolidatedMap.put(bookingOfferCargoEqualityStartingPoint, consolidatedCargoListStartingPointMap.get(bookingOfferCargoEqualityStartingPoint).first())
//      }
//    })
//
//    Collection<Cargo> consolidatedCargoCollection = allCargoConsolidatedMap.values()
//    return consolidatedCargoCollection
//  }

  private final Collection<Cargo> bookingOfferCargoCollection = [] as Collection<Cargo>

  private Quantity<Mass> totalCommodityWeight = 0.kg
  private BigDecimal totalContainerTeuCount = 0 // should be constrained to the max of, say 5000

  Collection<Cargo> getBookingOfferCargoCollection() {
    return Collections.unmodifiableCollection(bookingOfferCargoCollection)
  }

  /**
   * Primarily intended to be used from tests.
   */
  void checkCargoCollectionInvariants() {
    bookingOfferCargoCollection
        .groupBy({ Cargo existingCargo -> BookingOfferCargoEquality.fromCargo(existingCargo) })
        .entrySet()
        .each({ Map.Entry<BookingOfferCargoEquality, List<Cargo>> mapEntry -> requireTrue(mapEntry.value.size() == 1) })
  }

  Cargo findCargoByExample(Cargo cargoExample) {
    return findCargoByEquality(BookingOfferCargoEquality.fromCargo(cargoExample))
  }

  Cargo findCargoByEquality(BookingOfferCargoEquality cargoEquality) {
    return bookingOfferCargoCollection.find({ Cargo existingCargo -> BookingOfferCargoEquality.fromCargo(existingCargo) == cargoEquality })
  }

  Quantity<Mass> getTotalCommodityWeight() {
    return totalCommodityWeight
  }

  BigDecimal getTotalContainerTeuCount() {
    return totalContainerTeuCount
  }

  /**
   * Checks if we can accept addition of a {@link Cargo} collection at the {@link BookingOfferAggregate} level.
   * <p/>
   * We should use this method from the aggregate's command handler to check if it is valid to add the {@code Cargo} collection to the aggregate state. Actual state change happens later in the
   * event sourcing handler. Note that we cannot make this check in the event sourcing handler because it must make changes unconditionally to support rehydration from past events.
   */
  boolean canAcceptCargoCollectionAddition(Collection<Cargo> cargoCollectionToAdd, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    requireMatch(maxAllowedTeuCountPolicy, notNullValue())

    if (!cargoCollectionToAdd) {
      return true
    }

    Collection<Cargo> existingConsolidatedCargoCollection = bookingOfferCargoCollection
    Collection<Cargo> cargoCollectionWithAdditions = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, cargoCollectionToAdd)

    BigDecimal newTotalContainerTeuCount = 0
    cargoCollectionWithAdditions.each({ Cargo cargo -> newTotalContainerTeuCount = newTotalContainerTeuCount + cargo.containerTeuCount })

    return maxAllowedTeuCountPolicy.isTeuCountAllowed(newTotalContainerTeuCount)
  }

  /**
   * Without changing state of {@code BookingOfferCargos} instance, calculates totals in the same way as they will be calculated once the cargo is stored via
   * {@link #storeCargoCollectionAddition(java.util.Collection)}.
   * <p/>
   * This pre-calculation is used from the aggregate's command handler to calculate the totals required to create an event. The alternative would be to publish two events where the second one is
   * created based on state changes caused by the first event. However, we need pre-calculation as we want to publish a single event.
   * <p/>
   * This method is very similar to the {@link #calculateTotalsForCargoCollectionAddition(java.util.Collection)}, but this one also checks if we can accept the cargo addition.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> preCalculateTotalsForCargoCollectionAddition(Collection<Cargo> cargoCollectionToAdd, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    if (!canAcceptCargoCollectionAddition(cargoCollectionToAdd, maxAllowedTeuCountPolicy)) {
      throw new AssertionError("Cannot proceed with calculating totals since cargo is not acceptable." as Object)
    }

    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateTotalsForCargoCollectionAddition(cargoCollectionToAdd)
    return totalsTuple
  }

  // use this one in eventSourcingHandler to store past events unconditionally, regardless of potential change in previous business logic
  /**
   * Stores the cargo collection addition in the internal map by replacing any previously stored equivalent cargos.
   * <p/>
   * We should use this method only from the event sourcing handler as it changes the aggregate state and does it unconditionally without checking any invariants. This is necessary to support correct
   * rehydration of the aggregate from previous events. We should do all invariant checking in the aggregate's command handler.
   */
  void storeCargoCollectionAddition(Collection<Cargo> cargoCollectionToAdd) {
    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateTotalsForCargoCollectionAddition(cargoCollectionToAdd)
    totalCommodityWeight = totalsTuple.v1
    totalContainerTeuCount = totalsTuple.v2

    Collection<Cargo> existingConsolidatedCargoCollection = bookingOfferCargoCollection
    Collection<Cargo> consolidatedCargoCollection = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, cargoCollectionToAdd)

    consolidatedCargoCollection.each({ Cargo consolidatedCargo ->
      BookingOfferCargoEquality consolidatedCargoEquality = BookingOfferCargoEquality.fromCargo(consolidatedCargo)
      this.@bookingOfferCargoCollection.removeIf({ Cargo storedCargo -> BookingOfferCargoEquality.fromCargo(storedCargo) == consolidatedCargoEquality })
      this.@bookingOfferCargoCollection.add(consolidatedCargo)
    })
  }

  /**
   * Without changing the aggregate state, calculates new totals for cargo collection addition based on provided {@link Cargo} collection and the current aggregate state.
   * <p/>
   * This method is very similar to the {@link #preCalculateTotalsForCargoCollectionAddition(Collection, MaxAllowedTeuCountPolicy)}, but this one does not check if the cargo can be accepted or not.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   */
  Tuple2<Quantity<Mass>, BigDecimal> calculateTotalsForCargoCollectionAddition(Collection<Cargo> cargoCollectionToAdd) {
    Collection<Cargo> myCargoCollectionToAdd = cargoCollectionToAdd
    if (myCargoCollectionToAdd == null) {
      myCargoCollectionToAdd = []
    }

    Collection<Cargo> existingConsolidatedCargoCollection = bookingOfferCargoCollection
    Collection<Cargo> cargoCollectionWithAdditions = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, myCargoCollectionToAdd)

    Quantity<Mass> newTotalCommodityWeight = 0.kg
    BigDecimal newTotalContainerTeuCount = 0

    cargoCollectionWithAdditions.each({ Cargo consolidatedCargo ->
      newTotalCommodityWeight = newTotalCommodityWeight + consolidatedCargo.commodity.weight
      newTotalContainerTeuCount = newTotalContainerTeuCount + consolidatedCargo.containerTeuCount
    })

    return new Tuple2<Quantity<Mass>, BigDecimal>(newTotalCommodityWeight, newTotalContainerTeuCount)
  }
}
