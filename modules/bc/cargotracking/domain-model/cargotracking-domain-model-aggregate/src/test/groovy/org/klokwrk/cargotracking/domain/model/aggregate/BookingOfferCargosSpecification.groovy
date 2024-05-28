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

import org.klokwrk.cargotracking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.CargoFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.quantity.Mass

class BookingOfferCargosSpecification extends Specification {
  void "empty instance should be in the expected state"() {
    when:
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    then:
    bookingOfferCargos.bookingOfferCargoCollection.isEmpty()

    bookingOfferCargos.totalCommodityWeight == 0.kg
    bookingOfferCargos.totalContainerTeuCount == 0
  }

  void "consolidateCargoCollectionsForCargoAddition() should return empty collection for null or empty inputs"() {
    when:
    Collection<Cargo> consolidatedCargos = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargosParam, cargosToAddParam)

    then:
    consolidatedCargos.size() == 0

    where:
    cargosToAddParam | existingConsolidatedCargosParam
    null             | []
    []               | []
    null             | null
    []               | null
  }

  void "consolidateCargoCollectionsForCargoAddition() should work with empty cargosToAdd param"() {
    when:
    Collection<Cargo> consolidatedCargos = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargosParam, cargosToAddParam)

    then:
    consolidatedCargos != null
    consolidatedCargos.size() == existingConsolidatedCargosParam.size()
    consolidatedCargos.containsAll(existingConsolidatedCargosParam)

    where:
    cargosToAddParam | existingConsolidatedCargosParam
    null             | [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]
    []               | [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]
    null             | [CargoFixtureBuilder.cargo_dry().build()]
    []               | [CargoFixtureBuilder.cargo_dry().build()]
    null             | []
    []               | []
  }

  void "consolidateCargoCollectionsForCargoAddition() should work with empty or null existingConsolidatedCargos param"() {
    when:
    Collection<Cargo> consolidatedCargos = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargosParam, cargosToAddParam)

    then:
    consolidatedCargos.size() == cargosToAddParam.size()
    consolidatedCargos.containsAll(cargosToAddParam)

    where:
    existingConsolidatedCargosParam | cargosToAddParam
    null                            | [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]
    []                              | [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]
    null                            | [CargoFixtureBuilder.cargo_dry().build()]
    []                              | [CargoFixtureBuilder.cargo_dry().build()]
    null                            | []
    []                              | []
  }

  void "consolidateCargoCollectionsForCargoAddition() should throw when existingConsolidatedCargos param is not consolidated"() {
    given:
    Collection<Cargo> existingConsolidatedCargos_notReallyConsolidated = [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_dry().build()]

    when:
    BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargos_notReallyConsolidated, [CargoFixtureBuilder.cargo_dry().build()])

    then:
    thrown(AssertionError)
  }

  void "consolidateCargoCollectionsForCargoAddition() should work as expected - single type of cargo equality"() {
    given:
    Cargo cargoDry = CargoFixtureBuilder.cargo_dry().build()
    BookingOfferCargoEquality bookingOfferCargoDryEquality = BookingOfferCargoEquality.fromCargo(cargoDry)
    Collection<Cargo> cargosToAdd = [cargoDry, CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_dry().build()]

    when:
    Collection<Cargo> consolidatedCargos1 = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition([], cargosToAdd)

    then:
    consolidatedCargos1.size() == 1

    Cargo consolidatedCargoFound1 = consolidatedCargos1.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoDryEquality })
    verifyAll(consolidatedCargoFound1, {
      commodity.commodityType == cargoDry.commodity.commodityType
      commodity.requestedStorageTemperature == cargoDry.commodity.requestedStorageTemperature
      commodity.weight == cargoDry.commodity.weight * 3

      containerType == cargoDry.containerType
      maxAllowedWeightPerContainer == cargoDry.maxAllowedWeightPerContainer
      containerCount == 1
      containerTeuCount == 1
    })

    and:
    Collection<Cargo> existingConsolidatedCargos2 = [cargoDry]
    Commodity commodityToAdd2 = CommodityFixtureBuilder.dry_default().weightKg(25_000).build()
    Cargo cargoToAdd2 = CargoFixtureBuilder.cargo_dry().commodity(commodityToAdd2).build()
    Collection<Cargo> cargosToAdd2 = [cargoToAdd2]

    when:
    Collection<Cargo> consolidatedCargos2 = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargos2, cargosToAdd2)

    then:
    consolidatedCargos2.size() == 1

    Cargo consolidatedCargoFound2 = consolidatedCargos2.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoDryEquality })
    verifyAll(consolidatedCargoFound2, {
      commodity.commodityType == cargoDry.commodity.commodityType
      commodity.requestedStorageTemperature == cargoDry.commodity.requestedStorageTemperature
      commodity.weight == cargoDry.commodity.weight + cargoToAdd2.commodity.weight

      containerType == cargoDry.containerType
      maxAllowedWeightPerContainer == cargoDry.maxAllowedWeightPerContainer
      containerCount == 2
      containerTeuCount == 2
    })
  }

  void "consolidateCargoCollectionsForCargoAddition() should work as expected - multiple types of cargo equality"() {
    given:
    Cargo cargoDry = CargoFixtureBuilder.cargo_dry().build()
    BookingOfferCargoEquality bookingOfferCargoDryEquality = BookingOfferCargoEquality.fromCargo(cargoDry)

    Cargo cargoAirCooled = CargoFixtureBuilder.cargo_airCooled().build()
    BookingOfferCargoEquality bookingOfferCargoAirCooledEquality = BookingOfferCargoEquality.fromCargo(cargoAirCooled)

    Collection<Cargo> existingConsolidatedCargos = [cargoDry, cargoAirCooled]
    Collection<Cargo> cargosToAdd = [CargoFixtureBuilder.cargo_dry().build(), CargoFixtureBuilder.cargo_airCooled().build()]

    when:
    Collection<Cargo> consolidatedCargos = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargos, cargosToAdd)

    then:
    consolidatedCargos.size() == 2

    Cargo consolidatedCargoDryFound = consolidatedCargos.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoDryEquality })
    verifyAll(consolidatedCargoDryFound, {
      commodity.commodityType == cargoDry.commodity.commodityType
      commodity.requestedStorageTemperature == cargoDry.commodity.requestedStorageTemperature
      commodity.weight == cargoDry.commodity.weight * 2

      containerType == cargoDry.containerType
      maxAllowedWeightPerContainer == cargoDry.maxAllowedWeightPerContainer
      containerCount == 1
      containerTeuCount == 1
    })

    Cargo consolidatedCargoAirCooledFound = consolidatedCargos.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoAirCooledEquality })
    verifyAll(consolidatedCargoAirCooledFound, {
      commodity.commodityType == cargoAirCooled.commodity.commodityType
      commodity.requestedStorageTemperature == cargoAirCooled.commodity.requestedStorageTemperature
      commodity.weight == cargoAirCooled.commodity.weight * 2

      containerType == cargoAirCooled.containerType
      maxAllowedWeightPerContainer == cargoAirCooled.maxAllowedWeightPerContainer
      containerCount == 1
      containerTeuCount == 1
    })
  }

  void "consolidateCargoCollectionsForCargoAddition() should work as expected - varying requested storage temperature"() {
    given:
    Cargo cargoToAddTemp1 = CargoFixtureBuilder.cargo_airCooled().build()
    BookingOfferCargoEquality bookingOfferCargoTemp1Equality = BookingOfferCargoEquality.fromCargo(cargoToAddTemp1)

    Cargo cargoToAddTemp2 = CargoFixtureBuilder.cargo_airCooled().commodity(CommodityFixtureBuilder.airCooled_default().requestedStorageTemperatureDegC(10).build()).build()
    BookingOfferCargoEquality bookingOfferCargoTemp2Equality = BookingOfferCargoEquality.fromCargo(cargoToAddTemp2)

    Collection<Cargo> existingConsolidatedCargos = [CargoFixtureBuilder.cargo_airCooled().commodity(CommodityFixtureBuilder.airCooled_default().weightKg(2000).build()).build()]

    when:
    Collection<Cargo> consolidatedCargos = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(existingConsolidatedCargos, [cargoToAddTemp1, cargoToAddTemp2])

    then:
    consolidatedCargos.size() == 2

    Cargo consolidatedCargoTemp1Found = consolidatedCargos.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoTemp1Equality })
    verifyAll(consolidatedCargoTemp1Found, {
      commodity.commodityType == CommodityType.AIR_COOLED
      commodity.requestedStorageTemperature == CommodityType.AIR_COOLED.recommendedStorageTemperature
      commodity.weight == cargoToAddTemp1.commodity.weight * 3

      containerType == ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
      maxAllowedWeightPerContainer == ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight
      containerCount == 1
      containerTeuCount == 1
    })

    Cargo consolidatedCargoTemp2Found = consolidatedCargos.find({ Cargo cargo -> BookingOfferCargoEquality.fromCargo(cargo) == bookingOfferCargoTemp2Equality })
    verifyAll(consolidatedCargoTemp2Found, {
      commodity.commodityType == CommodityType.AIR_COOLED
      commodity.requestedStorageTemperature.value == 10
      commodity.weight == cargoToAddTemp2.commodity.weight

      containerType == ContainerType.TYPE_ISO_22R1_STANDARD_REEFER
      maxAllowedWeightPerContainer == ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight
      containerCount == 1
      containerTeuCount == 1
    })
  }

  void "canAcceptCargoCollectionAddition() method should throw for invalid parameters"() {
    when:
    BookingOfferCargos.canAcceptCargoCollectionAddition([], [], null)

    then:
    thrown(AssertionError)
  }

  void "canAcceptCargoCollectionAddition() method should work with empty cargoCollectionToAdd param"() {
    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition([], cargoCollectionToAddParam, new ConstantBasedMaxAllowedTeuCountPolicy(5000.0))

    then:
    canAcceptCargoResult

    where:
    cargoCollectionToAddParam | _
    null                      | _
    []                        | _
  }

  void "canAcceptCargoCollectionAddition() method should work for a single 10ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_12G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition([], [cargo], maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    10_010              | false
    10_001              | false
    10_000              | true
    9_999               | true
  }

  void "canAcceptCargoCollectionAddition() method should work for a multiple 10ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_12G1.maxCommodityWeight.value.toInteger()
    Cargo cargoToAdd1 = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, 1000 * containerTypeMaxCommodityWeight))
    Cargo cargoToAdd2 = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))

    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition([], [cargoToAdd1, cargoToAdd2], maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    9_010               | false
    9_001               | false
    9_000               | true
    8_999               | true
  }

  void "canAcceptCargoCollectionAddition() method should work for a multiple 10ft container with already existing cargo"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_12G1.maxCommodityWeight.value.toInteger()
    Cargo cargoExisting = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, 1000 * containerTypeMaxCommodityWeight))
    Cargo cargoToAdd1 = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, 1000 * containerTypeMaxCommodityWeight))
    Cargo cargoToAdd2 = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))

    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([cargoExisting])

    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition(bookingOfferCargos.bookingOfferCargoCollection, [cargoToAdd1, cargoToAdd2], maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    8_010               | false
    8_001               | false
    8_000               | true
    7_999               | true
  }

  void "canAcceptCargoCollectionAddition() method should work for 20ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition([], [cargo], maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    5010                | false
    5001                | false
    5000                | true
    4999                | true
  }

  void "canAcceptCargoCollectionAddition() method should work for 40ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_42G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_42G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    boolean canAcceptCargoResult = BookingOfferCargos.canAcceptCargoCollectionAddition([], [cargo], maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    2_510               | false
    2_501               | false
    2_500               | true
    2_499               | true
  }

  void "calculateTotalsForCargoCollectionAddition() method should work for empty cargoCollectionToAdd param"() {
    given:
    Cargo cargo = CargoFixtureBuilder.cargo_dry().build()
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([cargo])

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCargos.calculateTotalsForCargoCollectionAddition(cargoCollectionToAddParam)

    then:
    newTotals.v1 == cargo.commodity.weight
    newTotals.v2 == cargo.containerTeuCount

    where:
    cargoCollectionToAddParam | _
    null                      | _
    []                        | _
  }

  void "calculateTotalsForCargoCollectionAddition() method should work for empty BookingOfferCargos"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), 21_000.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCargos.calculateTotalsForCargoCollectionAddition([cargo])

    then:
    newTotals.v1 == 110_000.kg
    newTotals.v2 == 6
    bookingOfferCargos.bookingOfferCargoCollection.size() == 0
  }

  void "calculateTotalsForCargoCollectionAddition() method should work for non-empty BookingOfferCargos when calculating cargo of already stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), 21_000.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([cargo])
    assert bookingOfferCargos.totalCommodityWeight == 110_000.kg
    assert bookingOfferCargos.totalContainerTeuCount == 6

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCargos.calculateTotalsForCargoCollectionAddition([cargo])
    Quantity<Mass> newTotalCommodityWeight = newTotals.v1
    BigDecimal newTotalContainerTeuCount = newTotals.v2

    then:
    newTotalCommodityWeight == 220_000.kg
    newTotalContainerTeuCount == 11
    bookingOfferCargos.bookingOfferCargoCollection.size() == 1
  }

  void "calculateTotalsForCargoCollectionAddition() method should work for non-empty BookingOfferCargos when calculating cargo of not-already-stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), 21_000.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([cargo])
    assert bookingOfferCargos.totalCommodityWeight == 110_000.kg
    assert bookingOfferCargos.totalContainerTeuCount == 6

    Cargo nonStoredCargo = Cargo.make(ContainerType.TYPE_ISO_42R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 110_000), 24_500.kg)

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCargos.calculateTotalsForCargoCollectionAddition([nonStoredCargo])
    Quantity<Mass> newTotalCommodityWeight = newTotals.v1
    BigDecimal newTotalContainerTeuCount = newTotals.v2

    then:
    newTotalCommodityWeight == 220_000.kg
    newTotalContainerTeuCount == 16
    bookingOfferCargos.bookingOfferCargoCollection.size() == 1
  }

  void "preCalculateTotalsForCargoCollectionAddition() method should throw when cargo cannot be accepted"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.preCalculateTotalsForCargoCollectionAddition([cargo], maxAllowedTeuCountPolicy)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Cannot proceed with calculating totals since cargo is not acceptable."

    where:
    containerCountParam | _
    5010                | _
    5001                | _
  }

  void "preCalculateTotalsForCargoCollectionAddition() method should work for acceptable cargo"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCargos.preCalculateTotalsForCargoCollectionAddition([cargo], maxAllowedTeuCountPolicy)

    then:
    newTotals.v1 == (containerCountParam * 21_700).kg
    newTotals.v2 == containerCountParam
    bookingOfferCargos.bookingOfferCargoCollection.size() == 0

    where:
    containerCountParam | _
    4500                | _
    5000                | _
  }

  void "storeCargoCollectionAddition() should work for single cargo"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), 21_500.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo])

    then:
    noExceptionThrown()
    bookingOfferCargos.checkCargoCollectionInvariants()
    verifyAll(bookingOfferCargos, {
      totalContainerTeuCount == 10_000
      totalCommodityWeight == (10_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 1
      findCargoByExample(cargo).commodity.weight == cargo.commodity.weight
    })
  }

  void "storeCargoCollectionAddition() should work for multiple differentiated cargos"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), 21_500.kg)
    Cargo cargo2 = Cargo.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 10_000 * 21_500), 21_500.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo1, cargo2])

    then:
    noExceptionThrown()
    bookingOfferCargos.checkCargoCollectionInvariants()
    verifyAll(bookingOfferCargos, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == (20_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 2
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount
      findCargoByExample(cargo2).commodity.weight == cargo2.commodity.weight
      findCargoByExample(cargo2).containerTeuCount == cargo2.containerTeuCount
    })
  }

  void "storeCargoCollectionAddition() should work for multiple equivalent cargos"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), 21_500.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo1, cargo1])

    then:
    noExceptionThrown()
    bookingOfferCargos.checkCargoCollectionInvariants()
    verifyAll(bookingOfferCargos, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == (20_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 1
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight * 2
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount * 2
    })
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "storeCargoCollectionAddition() method should store multiple cargos correctly"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), 21_500.kg)
    Cargo cargo2 = Cargo.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 10_000 * 21_500), 21_500.kg)
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo1])

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCargos, {
      checkCargoCollectionInvariants()
      totalContainerTeuCount == 10_000
      totalCommodityWeight == (10_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 1
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount
    })

    and:
    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo2])

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCargos, {
      checkCargoCollectionInvariants()
      totalContainerTeuCount == 20_000
      totalCommodityWeight == (20_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 2
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount
      findCargoByExample(cargo2).commodity.weight == cargo2.commodity.weight
      findCargoByExample(cargo2).containerTeuCount == cargo2.containerTeuCount
    })

    and:
    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo1])

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCargos, {
      checkCargoCollectionInvariants()
      totalContainerTeuCount == 30_000
      totalCommodityWeight == (30_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 2
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight * 2
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount * 2
      findCargoByExample(cargo2).commodity.weight == cargo2.commodity.weight
      findCargoByExample(cargo2).containerTeuCount == cargo2.containerTeuCount
    })

    and:
    when:
    bookingOfferCargos.storeCargoCollectionAddition([cargo2])

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCargos, {
      checkCargoCollectionInvariants()
      totalContainerTeuCount == 40_000
      totalCommodityWeight == (40_000 * 21_500).kg
      bookingOfferCargoCollection.size() == 2
      findCargoByExample(cargo1).commodity.weight == cargo1.commodity.weight * 2
      findCargoByExample(cargo1).containerTeuCount == cargo1.containerTeuCount * 2
      findCargoByExample(cargo2).commodity.weight == cargo2.commodity.weight * 2
      findCargoByExample(cargo2).containerTeuCount == cargo2.containerTeuCount * 2
    })
  }

  void "checkCargoCollectionInvariants() method should not throw for empty bookingOfferCargoCollection"() {
    given:
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    when:
    bookingOfferCargos.checkCargoCollectionInvariants()

    then:
    noExceptionThrown()
  }

  void "checkCargoCollectionInvariants() method should not throw for consolidated bookingOfferCargoCollection"() {
    given:
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([CargoFixtureBuilder.cargo_dry().build()])

    when:
    bookingOfferCargos.checkCargoCollectionInvariants()

    then:
    noExceptionThrown()
  }

  void "checkCargoCollectionInvariants() method should throw for non-consolidated bookingOfferCargoCollection"() {
    given:
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()
    bookingOfferCargos.storeCargoCollectionAddition([CargoFixtureBuilder.cargo_dry().build()])

    //noinspection GroovyAccessibility
    bookingOfferCargos.@bookingOfferCargoCollection.add(CargoFixtureBuilder.cargo_dry().build())

    when:
    bookingOfferCargos.checkCargoCollectionInvariants()

    then:
    thrown(AssertionError)
  }
}
