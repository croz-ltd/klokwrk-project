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

import org.klokwrk.cargotracker.booking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass

class BookingOfferCargosSpecification extends Specification {
  void "empty instance should be in the expected state"() {
    when:
    BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

    then:
    bookingOfferCargos.bookingOfferCargoMap.isEmpty()

    bookingOfferCargos.totalCommodityWeight == Quantities.getQuantity(0, Units.KILOGRAM)
    bookingOfferCargos.totalContainerTeuCount == 0
  }

  void "canAcceptCargoAddition() method should work as expected for 10ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_12G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_12G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargoAddition(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    10_010              | false
    10_001              | false
    10_000              | true
    9_999               | true
  }

  void "canAcceptCargoAddition() method should work as expected for 20ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargoAddition(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    5010                | false
    5001                | false
    5000                | true
    4999                | true
  }

  void "canAcceptCargoAddition() method should work as expected for 40ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_42G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_42G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargoAddition(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    2_510               | false
    2_501               | false
    2_500               | true
    2_499               | true
  }

  void "calculateTotalsForCargoAddition() method should work as expected for empty BookingOfferCommodities"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateTotalsForCargoAddition(cargo)

    then:
    newTotals.v1 == Quantities.getQuantity(110_000, Units.KILOGRAM)
    newTotals.v2 == 6
    bookingOfferCommodities.bookingOfferCargoMap.size() == 0
  }

  void "calculateTotalsForCargoAddition() method should work as expected for non-empty BookingOfferCommodities when calculating cargo of already stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()
    bookingOfferCommodities.storeCargoAddition(cargo)
    assert bookingOfferCommodities.totalCommodityWeight == Quantities.getQuantity(110_000, Units.KILOGRAM)
    assert bookingOfferCommodities.totalContainerTeuCount == 6

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateTotalsForCargoAddition(cargo)
    Quantity<Mass> newTotalCommodityWeight = newTotals.v1
    BigDecimal newTotalContainerTeuCount = newTotals.v2

    then:
    newTotalCommodityWeight == Quantities.getQuantity(220_000, Units.KILOGRAM)
    newTotalContainerTeuCount == 11
    bookingOfferCommodities.bookingOfferCargoMap.size() == 1
  }

  void "calculateTotalsForCargoAddition() method should work as expected for non-empty BookingOfferCommodities when calculating cargo of not-already-stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()
    bookingOfferCommodities.storeCargoAddition(cargo)
    assert bookingOfferCommodities.totalCommodityWeight == Quantities.getQuantity(110_000, Units.KILOGRAM)
    assert bookingOfferCommodities.totalContainerTeuCount == 6

    Cargo nonStoredCargo = Cargo.make(ContainerType.TYPE_ISO_42R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 110_000), Quantities.getQuantity(24_500, Units.KILOGRAM))

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateTotalsForCargoAddition(nonStoredCargo)
    Quantity<Mass> newTotalCommodityWeight = newTotals.v1
    BigDecimal newTotalContainerTeuCount = newTotals.v2

    then:
    newTotalCommodityWeight == Quantities.getQuantity(220_000, Units.KILOGRAM)
    newTotalContainerTeuCount == 16
    bookingOfferCommodities.bookingOfferCargoMap.size() == 1
  }

  void "preCalculateTotalsForCargoAddition() method should throw when cargo cannot be accepted"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.preCalculateTotalsForCargoAddition(cargo, maxAllowedTeuCountPolicy)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Cannot proceed with calculating totals since cargo is not acceptable."

    where:
    containerCountParam | _
    5010                | _
    5001                | _
  }

  void "preCalculateTotalsForCargoAddition”() method should work as expected for acceptable cargo"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.preCalculateTotalsForCargoAddition(cargo, maxAllowedTeuCountPolicy)

    then:
    newTotals.v1 == Quantities.getQuantity(containerCountParam * 21_700, Units.KILOGRAM)
    newTotals.v2 == containerCountParam
    bookingOfferCommodities.bookingOfferCargoMap.size() == 0

    where:
    containerCountParam | _
    4500                | _
    5000                | _
  }

  void "storeCargoAddition() should work for single cargo"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey1 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo1)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.storeCargoAddition(cargo1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 10_000
      totalCommodityWeight == Quantities.getQuantity(10_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 1
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight
    })
  }

  void "storeCargoAddition() should work for multiple differentiated cargos"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey1 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo1)
    Cargo cargo2 = Cargo.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey2 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo2)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.storeCargoAddition(cargo1)
    bookingOfferCommodities.storeCargoAddition(cargo2)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == Quantities.getQuantity(20_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].commodity.weight == cargo2.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].containerTeuCount == cargo2.containerTeuCount
    })
  }

  void "storeCargoAddition() should work for multiple equivalent cargos"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey1 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo1)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.storeCargoAddition(cargo1)
    bookingOfferCommodities.storeCargoAddition(cargo1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == Quantities.getQuantity(20_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 1
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount * 2
    })
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "storeCargoAddition() method should store multiple cargos correctly"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey1 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo1)
    Cargo cargo2 = Cargo.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, Commodity.make(CommodityType.AIR_COOLED, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    String bookingOfferCargoMapKey2 = BookingOfferCargos.BookingOfferCargoMapKey.fromCargoAsString(cargo2)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.storeCargoAddition(cargo1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 10_000
      totalCommodityWeight == Quantities.getQuantity(10_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 1
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount
    })

    and:
    when:
    bookingOfferCommodities.storeCargoAddition(cargo2)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == Quantities.getQuantity(20_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].commodity.weight == cargo2.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].containerTeuCount == cargo2.containerTeuCount
    })

    and:
    when:
    bookingOfferCommodities.storeCargoAddition(cargo1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 30_000
      totalCommodityWeight == Quantities.getQuantity(30_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].commodity.weight == cargo2.commodity.weight
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].containerTeuCount == cargo2.containerTeuCount
    })

    and:
    when:
    bookingOfferCommodities.storeCargoAddition(cargo2)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 40_000
      totalCommodityWeight == Quantities.getQuantity(40_000 * 21_500, Units.KILOGRAM)
      bookingOfferCommodities.bookingOfferCargoMap.size() == 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].commodity.weight == cargo1.commodity.weight * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey1].containerTeuCount == cargo1.containerTeuCount * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].commodity.weight == cargo2.commodity.weight * 2
      bookingOfferCommodities.bookingOfferCargoMap[bookingOfferCargoMapKey2].containerTeuCount == cargo2.containerTeuCount * 2
    })
  }
}
