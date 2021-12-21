package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class CommodityTypeSpecification extends Specification {
  void "should have right count of elements"() {
    expect:
    CommodityType.values().size() == 4
  }

  void "isStorageTemperatureAllowed method should work as expected"() {
    given:
    CommodityType commodityType = CommodityType.valueOf(commodityTypeNameParam)

    when:
    Boolean isTemperatureAllowed = commodityType.isStorageTemperatureAllowed(Quantities.getQuantity(storageTemperatureNumberParam, Units.CELSIUS))

    then:
    isTemperatureAllowed == isTemperatureAllowedParam

    where:
    commodityTypeNameParam          | storageTemperatureNumberParam | isTemperatureAllowedParam
    CommodityType.DRY.name()        | 0                             | true
    CommodityType.DRY.name()        | -100                          | true
    CommodityType.DRY.name()        | 100                           | true

    CommodityType.AIR_COOLED.name() | 1                             | false
    CommodityType.AIR_COOLED.name() | 2                             | true
    CommodityType.AIR_COOLED.name() | 10                            | true
    CommodityType.AIR_COOLED.name() | 12                            | true
    CommodityType.AIR_COOLED.name() | 13                            | false

    CommodityType.CHILLED.name()    | -3                            | false
    CommodityType.CHILLED.name()    | -2                            | true
    CommodityType.CHILLED.name()    | 5                             | true
    CommodityType.CHILLED.name()    | 6                             | true
    CommodityType.CHILLED.name()    | 7                             | false

    CommodityType.FROZEN.name()     | -21                           | false
    CommodityType.FROZEN.name()     | -20                           | true
    CommodityType.FROZEN.name()     | -10                           | true
    CommodityType.FROZEN.name()     | -8                            | true
    CommodityType.FROZEN.name()     | -7                            | false
  }
}
