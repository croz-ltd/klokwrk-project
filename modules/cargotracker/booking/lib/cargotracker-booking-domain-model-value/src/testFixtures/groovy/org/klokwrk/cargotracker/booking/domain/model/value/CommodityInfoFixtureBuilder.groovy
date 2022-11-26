package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CommodityInfoFixtureBuilder {
  static CommodityInfoFixtureBuilder dry_default() {
    CommodityInfoFixtureBuilder commodityInfoFixtureBuilder = new CommodityInfoFixtureBuilder()
        .commodityType(CommodityType.DRY)
        .weightKg(1000)

    return commodityInfoFixtureBuilder
  }

  static CommodityInfoFixtureBuilder airCooled_default() {
    CommodityInfoFixtureBuilder commodityInfoFixtureBuilder = new CommodityInfoFixtureBuilder()
        .commodityType(CommodityType.AIR_COOLED)
        .weightKg(1000)

    return commodityInfoFixtureBuilder
  }

  static CommodityInfoFixtureBuilder chilled_default() {
    CommodityInfoFixtureBuilder commodityInfoFixtureBuilder = new CommodityInfoFixtureBuilder()
        .commodityType(CommodityType.CHILLED)
        .weightKg(1000)

    return commodityInfoFixtureBuilder
  }

  static CommodityInfoFixtureBuilder frozen_default() {
    CommodityInfoFixtureBuilder commodityInfoFixtureBuilder = new CommodityInfoFixtureBuilder()
        .commodityType(CommodityType.FROZEN)
        .weightKg(1000)

    return commodityInfoFixtureBuilder
  }

  CommodityType commodityType
  Integer weightKg
  Integer requestedStorageTemperatureDegC

  CommodityInfo build() {
    CommodityInfo commodityInfo = CommodityInfo.make(commodityType, weightKg, requestedStorageTemperatureDegC)
    return commodityInfo
  }
}
