/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.domain.model.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity

import javax.measure.Quantity
import javax.measure.quantity.Mass

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

  static Collection<Cargo> consolidateCargoCollectionsForCargoAddition(Collection<Cargo> existingConsolidatedCargos, Collection<Cargo> cargosToAdd) {
    Collection<Cargo> myExistingConsolidatedCargos = existingConsolidatedCargos ?: [] as Collection<Cargo>

    if (!cargosToAdd) {
      return myExistingConsolidatedCargos
    }

    checkIfCargoCollectionIsConsolidated(myExistingConsolidatedCargos)
    Map<BookingOfferCargoEquality, List<Cargo>> existingConsolidatedCargosMap = myExistingConsolidatedCargos.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })

    Map<BookingOfferCargoEquality, Cargo> allConsolidatedCargosMap = [:]

    // Note: Besides determining resultant consolidated map, we are also using cargosToAddMap as a base for determining maxAllowedWeightPerContainer to use
    Map<BookingOfferCargoEquality, List<Cargo>> cargosToAddMap = cargosToAdd.groupBy({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) })

    // First, add new entries that have equivalents (by BookingOfferCargoEquality) in the existing cargos
    cargosToAddMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityToAdd ->
      Collection<Quantity<Mass>> cargoCommodityWeightQuantities = cargosToAddMap.get(bookingOfferCargoEqualityToAdd)*.commodity.weight
      if (existingConsolidatedCargosMap.get(bookingOfferCargoEqualityToAdd) != null) {
        cargoCommodityWeightQuantities.addAll(existingConsolidatedCargosMap.get(bookingOfferCargoEqualityToAdd)*.commodity.weight as Collection<Quantity<Mass>>)
      }

      Quantity<Mass> totalCargoCommodityWeightQuantity = 0.kg
      cargoCommodityWeightQuantities.each { Quantity<Mass> cargoCommodityWeightQuantity -> totalCargoCommodityWeightQuantity = totalCargoCommodityWeightQuantity + cargoCommodityWeightQuantity }

      // Note: Selecting the cargoConsolidationBase element is significant because it determines maxAllowedWeightPerContainer to use with consolidated cargo. The maxAllowedWeightPerContainer must
      //       be aligned and produced by currently active MaxAllowedWeightPerContainerPolicy. Here we assume that all Cargo instances in input collection cargosToAdd are created with currently
      //       active MaxAllowedWeightPerContainerPolicy. The current MaxAllowedWeightPerContainerPolicy is used by CargoCreatorService when the command handlers of the aggregate convert input DTOs
      //       into real Cargo value objects.
      //       Also see the inline comment a few lines bellow related to the assumptions about policy changes.
      Cargo cargoConsolidationBase = cargosToAddMap.get(bookingOfferCargoEqualityToAdd).first()

      Commodity consolidatedCommodity = Commodity.make(cargoConsolidationBase.commodity.commodityType, totalCargoCommodityWeightQuantity, cargoConsolidationBase.commodity.requestedStorageTemperature)
      Cargo consolidatedCargo = Cargo.make(cargoConsolidationBase.containerType, consolidatedCommodity, cargoConsolidationBase.maxAllowedWeightPerContainer)
      allConsolidatedCargosMap.put(bookingOfferCargoEqualityToAdd, consolidatedCargo)
    })

    // Second, add starting point entries that are not changed because there are no equivalents to add
    existingConsolidatedCargosMap.keySet().each({ BookingOfferCargoEquality bookingOfferCargoEqualityStartingPoint ->
      if (!allConsolidatedCargosMap.containsKey(bookingOfferCargoEqualityStartingPoint)) {
        // Note: For not-changed cargos, we are not updating maxAllowedWeightPerContainer based on current MaxAllowedWeightPerContainerPolicy. This is ok as long as the active
        //       MaxAllowedWeightPerContainerPolicy does not change. If it does changes, any further modifications (through commands) of the BookingOfferAggregate should be prevented, and aggregate
        //       should go into effectively dormant state. However, there should be option for the user to create a new BookingOfferAggregate based on the dormant one, where all data should be copied
        //       but now with updated max allowed weights for each cargo type. Therefore, for the active BookingOfferAggregates, we are assuming that policies do not change and stay the same as they
        //       were during aggregate creation.
        allConsolidatedCargosMap.put(bookingOfferCargoEqualityStartingPoint, existingConsolidatedCargosMap.get(bookingOfferCargoEqualityStartingPoint).first())
      }
    })

    Collection<Cargo> consolidatedCargoCollection = allConsolidatedCargosMap.values()
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

  /**
   * Checks if we can accept addition of a {@link Cargo} collection at the existing consolidated cargo collection.
   * <p/>
   * Intended to be used to verify potential cargo addition at the level of the {@link BookingOfferAggregate} instance.
   * <p/>
   * We should use this method from the aggregate's command handlers to check if it is valid to add the cargo collection to the aggregate state. Actual state change happens later in the
   * event sourcing handler. Note that we cannot make this check in the event sourcing handler because it must make changes unconditionally to support rehydration from past events.
   */
  static boolean canAcceptCargoCollectionAddition(Collection<Cargo> existingConsolidatedCargoCollection, Collection<Cargo> cargoCollectionToAdd, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy) {
    assert maxAllowedTeuCountPolicy != null

    if (!cargoCollectionToAdd) {
      return true
    }

    Collection<Cargo> cargoCollectionWithAdditions = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, cargoCollectionToAdd)

    BigDecimal newTotalContainerTeuCount = 0
    cargoCollectionWithAdditions.each({ Cargo cargo -> newTotalContainerTeuCount = newTotalContainerTeuCount + cargo.containerTeuCount })

    return maxAllowedTeuCountPolicy.isTeuCountAllowed(newTotalContainerTeuCount)
  }

  /**
   * Calculates commodity weight and container TEU count totals of the existing consolidated cargo collection if additional cargo collection is added to it.
   * <p/>
   * The method returns a tuple of 2 where value v1 is the new {@code totalCommodityWeight} and value v2 is the new {@code totalContainerTeuCount}.
   * <p/>
   * Before executing this method from the command handler, one should check if cargo can be accepted at all according to the current MaxAllowedTeuCountPolicy. One can use
   * {@link #canAcceptCargoCollectionAddition(Collection, Collection, MaxAllowedTeuCountPolicy)} for that purpose.
   */
  static Tuple2<Quantity<Mass>, BigDecimal> calculateTotalsForCargoCollectionAddition(Collection<Cargo> existingConsolidatedCargoCollection, Collection<Cargo> cargoCollectionToAdd) {
    Collection<Cargo> myCargoCollectionToAdd = cargoCollectionToAdd
    if (myCargoCollectionToAdd == null) {
      myCargoCollectionToAdd = []
    }

    Collection<Cargo> cargoCollectionWithAdditions = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, myCargoCollectionToAdd)

    Quantity<Mass> newTotalCommodityWeight = 0.kg
    BigDecimal newTotalContainerTeuCount = 0

    cargoCollectionWithAdditions.each({ Cargo consolidatedCargo ->
      newTotalCommodityWeight = newTotalCommodityWeight + consolidatedCargo.commodity.weight
      newTotalContainerTeuCount = newTotalContainerTeuCount + consolidatedCargo.containerTeuCount
    })

    return new Tuple2<Quantity<Mass>, BigDecimal>(newTotalCommodityWeight, newTotalContainerTeuCount)
  }

  static void checkIfCargoCollectionIsConsolidated(Collection<Cargo> existingConsolidatedCargoCollection) {
    assert existingConsolidatedCargoCollection != null
    existingConsolidatedCargoCollection
        .groupBy({ Cargo existingCargo -> BookingOfferCargoEquality.fromCargo(existingCargo) })
        .entrySet()
        .each({ Map.Entry<BookingOfferCargoEquality, List<Cargo>> mapEntry -> assert mapEntry.value.size() == 1 })
  }

  Collection<Cargo> bookingOfferCargoCollection = [] as Collection<Cargo>
  private Quantity<Mass> totalCommodityWeight = 0.kg
  private BigDecimal totalContainerTeuCount = 0 // should be constrained to the max of, say 5000

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

  // use this one in eventSourcingHandler to store past events unconditionally, regardless of potential change in previous business logic
  /**
   * Stores the cargo collection addition in the internal map by replacing any previously stored equivalent cargos.
   * <p/>
   * We should use this method only from the event sourcing handler as it changes the aggregate state and does it unconditionally without checking any invariants. This is necessary to support correct
   * rehydration of the aggregate from previous events. We should do all invariant checking in the aggregate's command handler.
   */
  void storeCargoCollectionAddition(Collection<Cargo> cargoCollectionToAdd) {
    Tuple2<Quantity<Mass>, BigDecimal> totalsTuple = calculateTotalsForCargoCollectionAddition(bookingOfferCargoCollection, cargoCollectionToAdd)
    totalCommodityWeight = totalsTuple.v1
    totalContainerTeuCount = totalsTuple.v2

    Collection<Cargo> existingConsolidatedCargoCollection = bookingOfferCargoCollection
    Collection<Cargo> consolidatedCargoCollection = consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargoCollection, cargoCollectionToAdd)
    bookingOfferCargoCollection.clear()
    bookingOfferCargoCollection.addAll(consolidatedCargoCollection)
  }
}
