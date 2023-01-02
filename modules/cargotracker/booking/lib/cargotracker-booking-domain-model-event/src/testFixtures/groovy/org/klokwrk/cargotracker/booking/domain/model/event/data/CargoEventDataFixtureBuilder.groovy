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
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CargoEventDataFixtureBuilder {
  static CargoEventDataFixtureBuilder cargo_dry() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.DRY)
        .commodityRequestedStorageTemperature(null)
        .commodityWeight(Quantities.getQuantity(1000, Units.KILOGRAM))
        .containerType(ContainerType.TYPE_ISO_22G1)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22G1.maxCommodityWeight)

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_airCooled() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.AIR_COOLED)
        .commodityRequestedStorageTemperature(CommodityType.AIR_COOLED.recommendedStorageTemperature)
        .commodityWeight(Quantities.getQuantity(1000, Units.KILOGRAM))
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight)

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_chilled() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.CHILLED)
        .commodityRequestedStorageTemperature(CommodityType.CHILLED.recommendedStorageTemperature)
        .commodityWeight(Quantities.getQuantity(1000, Units.KILOGRAM))
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight)

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_frozen() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.FROZEN)
        .commodityRequestedStorageTemperature(CommodityType.FROZEN.recommendedStorageTemperature)
        .commodityWeight(Quantities.getQuantity(1000, Units.KILOGRAM))
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight)

    return cargoEventDataFixtureBuilder
  }

  CommodityType commodityType
  Quantity<Temperature> commodityRequestedStorageTemperature
  Quantity<Mass> commodityWeight
  ContainerType containerType
  Quantity<Mass> maxAllowedWeightPerContainer

  CargoEventData build() {
    CargoEventData cargoEventData = CargoEventData.fromCargo(Cargo.make(
        containerType,
        Commodity.make(
            commodityType,
            commodityWeight,
            commodityRequestedStorageTemperature == null ? null : commodityRequestedStorageTemperature
        ),
        maxAllowedWeightPerContainer
    ))

    return cargoEventData
  }
}
