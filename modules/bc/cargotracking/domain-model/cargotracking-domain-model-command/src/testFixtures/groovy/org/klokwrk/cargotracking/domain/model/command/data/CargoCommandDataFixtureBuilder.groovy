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
package org.klokwrk.cargotracking.domain.model.command.data

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityFixtureBuilder
import org.klokwrk.cargotracking.domain.model.value.ContainerDimensionType

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CargoCommandDataFixtureBuilder {
  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static CargoCommandDataFixtureBuilder createCargoCommandData_default() {
    CargoCommandDataFixtureBuilder builder = new CargoCommandDataFixtureBuilder()
        .commodity(CommodityFixtureBuilder.dry_default().build())
        .containerDimensionType(ContainerDimensionType.DIMENSION_ISO_22)

    return builder
  }

  Commodity commodity
  ContainerDimensionType containerDimensionType

  CargoCommandData build() {
    CargoCommandData cargoCommandData = new CargoCommandData(
        commodity: commodity,
        containerDimensionType: containerDimensionType
    )

    return cargoCommandData
  }
}
