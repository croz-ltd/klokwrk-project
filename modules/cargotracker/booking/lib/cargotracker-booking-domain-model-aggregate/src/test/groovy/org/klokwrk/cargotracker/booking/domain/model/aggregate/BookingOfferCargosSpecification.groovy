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
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
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
    bookingOfferCargos.commodityTypeToCargoMap.isEmpty()

    bookingOfferCargos.totalCommodityWeight == Quantities.getQuantity(0, Units.KILOGRAM)
    bookingOfferCargos.totalContainerTeuCount == 0
  }

  void "canAcceptCargo() method should work as expected for 10ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_12G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_12G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargo(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    10_010              | false
    10_001              | false
    10_000              | true
    9_999               | true
  }

  void "canAcceptCargo() method should work as expected for 20ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargo(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    5010                | false
    5001                | false
    5000                | true
    4999                | true
  }

  void "canAcceptCargo() method should work as expected for 40ft container"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_42G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_42G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    boolean canAcceptCargoResult = bookingOfferCommodities.canAcceptCargo(cargo, maxAllowedTeuCountPolicy)

    then:
    canAcceptCargoResult == canAcceptCargoResultParam

    where:
    containerCountParam | canAcceptCargoResultParam
    2_510               | false
    2_501               | false
    2_500               | true
    2_499               | true
  }

  void "calculateNewTotals() method should work as expected for empty BookingOfferCommodities"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateNewTotals(cargo)

    then:
    newTotals.v1 == Quantities.getQuantity(110_000, Units.KILOGRAM)
    newTotals.v2 == 6
  }

  void "calculateNewTotals() method should work as expected for non-empty BookingOfferCommodities when calculating cargo of already stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()
    bookingOfferCommodities.storeCargo(cargo)

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateNewTotals(cargo)

    then:
    newTotals.v1 == Quantities.getQuantity(110_000, Units.KILOGRAM)
    newTotals.v2 == 6
  }

  void "calculateNewTotals() method should work as expected for non-empty BookingOfferCommodities when calculating cargo of not-already-stored commodity type"() {
    given:
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(21_000, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()
    bookingOfferCommodities.storeCargo(cargo)
    Cargo nonStoredCargo = Cargo.make(ContainerType.TYPE_ISO_42R1_STANDARD_REEFER, CommodityInfo.make(CommodityType.AIR_COOLED, 110_000), Quantities.getQuantity(24_500, Units.KILOGRAM))

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.calculateNewTotals(nonStoredCargo)

    then:
    newTotals.v1 == Quantities.getQuantity(220_000, Units.KILOGRAM)
    newTotals.v2 == 16
  }

  void "preCalculateTotals() method should throw when cargo cannot be accepted"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.preCalculateTotals(cargo, maxAllowedTeuCountPolicy)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Cannot proceed with calculating totals since cargo is not acceptable."

    where:
    containerCountParam | _
    5010                | _
    5001                | _
  }

  void "preCalculateTotals() method should work as expected for acceptable cargo"() {
    given:
    Integer containerTypeMaxCommodityWeight = ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value.toInteger()
    Cargo cargo = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * containerTypeMaxCommodityWeight))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    Tuple2<Quantity<Mass>, BigDecimal> newTotals = bookingOfferCommodities.preCalculateTotals(cargo, maxAllowedTeuCountPolicy)

    then:
    newTotals.v1 == Quantities.getQuantity(containerCountParam * 21_700, Units.KILOGRAM)
    newTotals.v2 == containerCountParam

    where:
    containerCountParam | _
    4500                | _
    5000                | _
  }

  void "storeCargo() method should store cargo unconditionally"() {
    given:
    Cargo cargo1 = Cargo.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    Cargo cargo2 = Cargo.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, CommodityInfo.make(CommodityType.AIR_COOLED, 10_000 * 21_500), Quantities.getQuantity(21_500, Units.KILOGRAM))
    BookingOfferCargos bookingOfferCommodities = new BookingOfferCargos()

    when:
    bookingOfferCommodities.storeCargo(cargo1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 10_000
      totalCommodityWeight == Quantities.getQuantity(10_000 * 21_500, Units.KILOGRAM)
    })

    and:
    when:
    bookingOfferCommodities.storeCargo(cargo2)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerTeuCount == 20_000
      totalCommodityWeight == Quantities.getQuantity(20_000 * 21_500, Units.KILOGRAM)
    })
  }
}
