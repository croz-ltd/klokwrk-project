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

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CommodityFixtureBuilder {
  static CommodityFixtureBuilder dry_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .commodityType(CommodityType.DRY)
        .weightKg(1000)

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder airCooled_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .commodityType(CommodityType.AIR_COOLED)
        .weightKg(1000)

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder chilled_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .commodityType(CommodityType.CHILLED)
        .weightKg(1000)

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder frozen_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .commodityType(CommodityType.FROZEN)
        .weightKg(1000)

    return commodityFixtureBuilder
  }

  CommodityType commodityType
  Integer weightKg
  Integer requestedStorageTemperatureDegC

  Commodity build() {
    Commodity commodity = Commodity.make(commodityType, weightKg, requestedStorageTemperatureDegC)
    return commodity
  }
}
