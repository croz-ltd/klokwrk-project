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
package org.klokwrk.cargotracking.domain.model.value

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import javax.measure.Quantity
import javax.measure.quantity.Mass

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CargoFixtureBuilder {
  static CargoFixtureBuilder cargo_dry() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22G1)
        .commodity(CommodityFixtureBuilder.dry_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_airCooled() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodity(CommodityFixtureBuilder.airCooled_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_chilled() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodity(CommodityFixtureBuilder.chilled_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_frozen() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodity(CommodityFixtureBuilder.frozen_default().build())

    return cargoFixtureBuilder
  }

  ContainerType containerType
  Commodity commodity
  Integer maxAllowedWeightPerContainerKg

  Cargo build() {
    Quantity<Mass> maxAllowedWeightPerContainerToUse = null
    if (maxAllowedWeightPerContainerKg != null) {
      maxAllowedWeightPerContainerToUse = maxAllowedWeightPerContainerKg.kg
    }

    Cargo cargo = Cargo.make(containerType, commodity, maxAllowedWeightPerContainerToUse)
    return cargo
  }
}
