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
package org.klokwrk.cargotracking.domain.model.event

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Clock

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CargoAddedEventFixtureBuilder {
  static CargoAddedEventFixtureBuilder builder(String bookingOfferId = null) {
    CargoAddedEventFixtureBuilder builder = new CargoAddedEventFixtureBuilder()

    if (bookingOfferId) {
      builder.bookingOfferId(bookingOfferId)
    }

    return builder
  }

  static CargoAddedEventFixtureBuilder cargoAddedEvent_default(Clock currentTimeClock = Clock.systemUTC()) {
    CargoEventData cargo = CargoEventDataFixtureBuilder.cargo_dry().build()

    CargoAddedEventFixtureBuilder builder = new CargoAddedEventFixtureBuilder()
        .bookingOfferId(CombUuidShortPrefixUtils.makeCombShortPrefix(currentTimeClock).toString())
        .cargo(cargo)
        .totalCommodityWeight(cargo.commodityWeight)
        .totalContainerTeuCount(cargo.containerTeuCount)

    return builder
  }

  String bookingOfferId
  CargoEventData cargo
  Quantity<Mass> totalCommodityWeight
  BigDecimal totalContainerTeuCount

  CargoAddedEventFixtureBuilder recalculateTotals() {
    if (cargo) {
      totalCommodityWeight = cargo.commodityWeight
      totalContainerTeuCount = cargo.containerTeuCount
    }

    return this
  }

  CargoAddedEvent build() {
    if (!totalCommodityWeight || !totalContainerTeuCount) {
      recalculateTotals()
    }

    CargoAddedEvent cargoAddedEvent = new CargoAddedEvent(bookingOfferId: bookingOfferId, cargo: cargo, totalCommodityWeight: totalCommodityWeight, totalContainerTeuCount: totalContainerTeuCount)
    return cargoAddedEvent
  }
}
