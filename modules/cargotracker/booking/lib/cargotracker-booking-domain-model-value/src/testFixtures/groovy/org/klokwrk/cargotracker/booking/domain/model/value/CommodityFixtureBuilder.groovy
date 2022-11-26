package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CommodityFixtureBuilder {
  static CommodityFixtureBuilder dry_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22G1)
        .commodityInfo(CommodityInfoFixtureBuilder.dry_default().build())

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder airCooled_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.airCooled_default().build())

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder chilled_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.chilled_default().build())

    return commodityFixtureBuilder
  }

  static CommodityFixtureBuilder frozen_default() {
    CommodityFixtureBuilder commodityFixtureBuilder = new CommodityFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.frozen_default().build())

    return commodityFixtureBuilder
  }

  ContainerType containerType
  CommodityInfo commodityInfo
  Integer maxAllowedWeightPerContainerKg

  Commodity build() {
    Quantity<Mass> maxAllowedWeightPerContainerToUse = null
    if (maxAllowedWeightPerContainerKg != null) {
      maxAllowedWeightPerContainerToUse = Quantities.getQuantity(maxAllowedWeightPerContainerKg, Units.KILOGRAM)
    }

    Commodity commodity = Commodity.make(containerType, commodityInfo, maxAllowedWeightPerContainerToUse)
    return commodity
  }
}
