package org.klokwrk.cargotracker.booking.domain.model.value

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
