package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class CommodityTypeSpecification extends Specification {
  void "should have right count of elements"() {
    expect:
    CommodityType.values().size() == 3
  }

  void "isStorageTemperatureAllowed method should work as expected"() {
    given:
    CommodityType commodityType = CommodityType.valueOf(commodityTypeNameParam)

    when:
    Boolean isTemperatureAllowed = commodityType.isStorageTemperatureAllowed(Quantities.getQuantity(storageTemperatureNumberParam, Units.CELSIUS))

    then:
    isTemperatureAllowed == isTemperatureAllowedParam

    where:
    commodityTypeNameParam       | storageTemperatureNumberParam | isTemperatureAllowedParam
    CommodityType.DRY.name()     | 0                             | true
    CommodityType.DRY.name()     | -100                          | true
    CommodityType.DRY.name()     | 100                           | true

    CommodityType.CHILLED.name() | -1                            | false
    CommodityType.CHILLED.name() | 0                             | true
    CommodityType.CHILLED.name() | 5                             | true
    CommodityType.CHILLED.name() | 8                             | true
    CommodityType.CHILLED.name() | 9                             | false

    CommodityType.FROZEN.name()  | -36                           | false
    CommodityType.FROZEN.name()  | -35                           | true
    CommodityType.FROZEN.name()  | -10                           | true
    CommodityType.FROZEN.name()  | -1                            | true
    CommodityType.FROZEN.name()  | 0                             | false
  }
}
