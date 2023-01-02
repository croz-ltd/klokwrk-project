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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

@SuppressWarnings("CodeNarc.DuplicateImport")
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class CargoEventData {
  CommodityType commodityType
  Quantity<Temperature> commodityRequestedStorageTemperature
  Quantity<Mass> commodityWeight
  Integer containerCount
  BigDecimal containerTeuCount
  ContainerType containerType
  Quantity<Mass> maxAllowedWeightPerContainer
  Quantity<Mass> maxRecommendedWeightPerContainer

  static Collection<CargoEventData> fromCargoCollection(Collection<Cargo> cargoCollection) {
    return cargoCollection.collect({ Cargo commodity -> fromCargo(commodity) })
  }

  static CargoEventData fromCargo(Cargo cargo) {
    return new CargoEventData(
        commodityType: cargo.commodity.commodityType,
        commodityRequestedStorageTemperature: cargo.commodity.requestedStorageTemperature == null ? null : cargo.commodity.requestedStorageTemperature,
        commodityWeight: cargo.commodity.weight,
        containerCount: cargo.containerCount,
        containerTeuCount: cargo.containerTeuCount,
        containerType: cargo.containerType,
        maxAllowedWeightPerContainer: cargo.maxAllowedWeightPerContainer,
        maxRecommendedWeightPerContainer: cargo.maxRecommendedWeightPerContainer
    )
  }

  static Collection<Cargo> toCargoCollection(Collection<CargoEventData> cargoEventDataCollection) {
    return cargoEventDataCollection.collect({ CargoEventData cargoEventData -> cargoEventData.toCargo() })
  }

  Cargo toCargo() {
    Cargo cargo = Cargo.make(
        containerType,
        Commodity.make(
            commodityType,
            commodityWeight,
            commodityRequestedStorageTemperature == null ? null : commodityRequestedStorageTemperature
        ),
        maxAllowedWeightPerContainer
    )

    return cargo
  }
}
