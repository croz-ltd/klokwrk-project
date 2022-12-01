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
class CargoFixtureBuilder {
  static CargoFixtureBuilder cargo_dry() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22G1)
        .commodityInfo(CommodityInfoFixtureBuilder.dry_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_airCooled() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.airCooled_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_chilled() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.chilled_default().build())

    return cargoFixtureBuilder
  }

  static CargoFixtureBuilder cargo_frozen() {
    CargoFixtureBuilder cargoFixtureBuilder = new CargoFixtureBuilder()
        .containerType(ContainerType.TYPE_ISO_22R1_STANDARD_REEFER)
        .commodityInfo(CommodityInfoFixtureBuilder.frozen_default().build())

    return cargoFixtureBuilder
  }

  ContainerType containerType
  CommodityInfo commodityInfo
  Integer maxAllowedWeightPerContainerKg

  Cargo build() {
    Quantity<Mass> maxAllowedWeightPerContainerToUse = null
    if (maxAllowedWeightPerContainerKg != null) {
      maxAllowedWeightPerContainerToUse = Quantities.getQuantity(maxAllowedWeightPerContainerKg, Units.KILOGRAM)
    }

    Cargo cargo = Cargo.make(containerType, commodityInfo, maxAllowedWeightPerContainerToUse)
    return cargo
  }
}
