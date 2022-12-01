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
      maxAllowedWeightPerContainerToUse = Quantities.getQuantity(maxAllowedWeightPerContainerKg, Units.KILOGRAM)
    }

    Cargo cargo = Cargo.make(containerType, commodity, maxAllowedWeightPerContainerToUse)
    return cargo
  }
}
