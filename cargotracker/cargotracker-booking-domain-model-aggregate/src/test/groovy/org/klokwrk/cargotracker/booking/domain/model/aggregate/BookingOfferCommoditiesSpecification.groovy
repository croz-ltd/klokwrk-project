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

import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass

class BookingOfferCommoditiesSpecification extends Specification {
  void "empty instance should be in the expected state"() {
    when:
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    then:
    bookingOfferCommodities.commodityTypeToCommodityMap.isEmpty()

    bookingOfferCommodities.totalCommodityWeight == Quantities.getQuantity(0, Units.KILOGRAM)
    bookingOfferCommodities.totalContainerCount == 0
  }

  void "canAcceptCommodity() method should work as expected"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * 25_000), Quantities.getQuantity(25_000, Units.KILOGRAM))

    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    when:
    Boolean canAcceptCommodityResult = bookingOfferCommodities.canAcceptCommodity(commodity)

    then:
    canAcceptCommodityResult == canAcceptCommodityResultParam

    where:
    containerCountParam | canAcceptCommodityResultParam
    5010                | false
    5001                | false
    5000                | true
    4999                | true
  }

  void "calculateNewTotals() method should work as expected for empty BookingOfferCommodities"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(24_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    when:
    Tuple2<Quantity<Mass>, Integer> newTotals = bookingOfferCommodities.calculateNewTotals(commodity)

    then:
    newTotals.v1 == Quantities.getQuantity(110_000, Units.KILOGRAM)
    newTotals.v2 == 5
  }

  void "calculateNewTotals() method should work as expected for non-empty BookingOfferCommodities when calculating commodity of already stored commodity type"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(24_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()
    bookingOfferCommodities.storeCommodity(commodity)

    when:
    Tuple2<Quantity<Mass>, Integer> newTotals = bookingOfferCommodities.calculateNewTotals(commodity)

    then:
    newTotals.v1 == Quantities.getQuantity(110_000, Units.KILOGRAM)
    newTotals.v2 == 5
  }

  void "calculateNewTotals() method should work as expected for non-empty BookingOfferCommodities when calculating commodity of not-already-stored commodity type"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 110_000), Quantities.getQuantity(24_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()
    bookingOfferCommodities.storeCommodity(commodity)
    Commodity nonStoredCommodity = Commodity.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, CommodityInfo.make(CommodityType.AIR_COOLED, 110_000), Quantities.getQuantity(24_000, Units.KILOGRAM))

    when:
    Tuple2<Quantity<Mass>, Integer> newTotals = bookingOfferCommodities.calculateNewTotals(nonStoredCommodity)

    then:
    newTotals.v1 == Quantities.getQuantity(220_000, Units.KILOGRAM)
    newTotals.v2 == 10
  }

  void "preCalculateTotals() method should throw when commodity cannot be accepted"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * 25_000), Quantities.getQuantity(25_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    when:
    bookingOfferCommodities.preCalculateTotals(commodity)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Cannot proceed with calculating totals since commodity is not acceptable."

    where:
    containerCountParam | _
    5010                | _
    5001                | _
  }

  void "preCalculateTotals() method should work as expected for acceptable commodity"() {
    given:
    Commodity commodity = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, containerCountParam * 25_000), Quantities.getQuantity(25_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    when:
    Tuple2<Quantity<Mass>, Integer> newTotals = bookingOfferCommodities.preCalculateTotals(commodity)

    then:
    newTotals.v1 == Quantities.getQuantity(containerCountParam * 25_000, Units.KILOGRAM)
    newTotals.v2 == containerCountParam

    where:
    containerCountParam | _
    4500                | _
    5000                | _
  }

  void "storeCommodity() method should store commodity unconditionally"() {
    given:
    Commodity commodity1 = Commodity.make(ContainerType.TYPE_ISO_22G1, CommodityInfo.make(CommodityType.DRY, 10_000 * 25_000), Quantities.getQuantity(25_000, Units.KILOGRAM))
    Commodity commodity2 = Commodity.make(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER, CommodityInfo.make(CommodityType.AIR_COOLED, 10_000 * 25_000), Quantities.getQuantity(25_000, Units.KILOGRAM))
    BookingOfferCommodities bookingOfferCommodities = new BookingOfferCommodities()

    when:
    bookingOfferCommodities.storeCommodity(commodity1)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerCount == 10_000
      totalCommodityWeight == Quantities.getQuantity(10_000 * 25_000, Units.KILOGRAM)
    })

    and:
    when:
    bookingOfferCommodities.storeCommodity(commodity2)

    then:
    noExceptionThrown()
    verifyAll(bookingOfferCommodities, {
      totalContainerCount == 20_000
      totalCommodityWeight == Quantities.getQuantity(20_000 * 25_000, Units.KILOGRAM)
    })
  }
}
