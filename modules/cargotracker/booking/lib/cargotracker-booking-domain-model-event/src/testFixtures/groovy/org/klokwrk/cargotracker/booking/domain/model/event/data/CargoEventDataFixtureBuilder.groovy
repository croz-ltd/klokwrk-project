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
package org.klokwrk.cargotracker.booking.domain.model.event.data

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType

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
        .commodityWeight("1000 kg")
        .containerType(ContainerType.TYPE_ISO_22G1)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22G1.maxCommodityWeight.value + " kg")

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_airCooled() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.AIR_COOLED)
        .commodityRequestedStorageTemperature(QuantityFormatter.instance.format(CommodityType.AIR_COOLED.recommendedStorageTemperature))
        .commodityWeight("1000 kg")
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight.value + " kg")

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_chilled() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.CHILLED)
        .commodityRequestedStorageTemperature(QuantityFormatter.instance.format(CommodityType.CHILLED.recommendedStorageTemperature))
        .commodityWeight("1000 kg")
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight.value + " kg")

    return cargoEventDataFixtureBuilder
  }

  static CargoEventDataFixtureBuilder cargo_frozen() {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = new CargoEventDataFixtureBuilder()
        .commodityType(CommodityType.FROZEN)
        .commodityRequestedStorageTemperature(QuantityFormatter.instance.format(CommodityType.FROZEN.recommendedStorageTemperature))
        .commodityWeight("1000 kg")
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER.maxCommodityWeight.value + " kg")

    return cargoEventDataFixtureBuilder
  }

  CommodityType commodityType
  String commodityRequestedStorageTemperature
  String commodityWeight
  ContainerType containerType
  String maxAllowedWeightPerContainer

  CargoEventData build() {
    CargoEventData cargoEventData = CargoEventData.fromCargo(Cargo.make(
        containerType,
        CommodityInfo.make(
            commodityType,
            QuantityFormatter.instance.parse(commodityWeight) as Quantity<Mass>,
            commodityRequestedStorageTemperature == null ? null : QuantityFormatter.instance.parse(commodityRequestedStorageTemperature) as Quantity<Temperature>
        ),
        QuantityFormatter.instance.parse(maxAllowedWeightPerContainer) as Quantity<Mass>
    ))

    return cargoEventData
  }
}
