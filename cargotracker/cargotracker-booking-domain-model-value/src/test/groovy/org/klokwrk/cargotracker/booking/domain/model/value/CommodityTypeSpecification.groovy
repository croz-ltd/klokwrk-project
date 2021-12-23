package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.AIR_COOLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.CHILLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.FROZEN

class CommodityTypeSpecification extends Specification {
  void "should have expected enum size"() {
    expect:
    CommodityType.values().size() == 4
  }

  void "isStorageTemperatureAllowed method should work as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam

    when:
    Boolean isTemperatureAllowed = commodityType.isStorageTemperatureAllowed(Quantities.getQuantity(storageTemperatureNumberParam, Units.CELSIUS))

    then:
    isTemperatureAllowed == isTemperatureAllowedParam

    where:
    commodityTypeParam | storageTemperatureNumberParam | isTemperatureAllowedParam
    DRY                | 0                             | true
    DRY                | -100                          | true
    DRY                | 100                           | true

    AIR_COOLED         | 1                             | false
    AIR_COOLED         | 2                             | true
    AIR_COOLED         | 10                            | true
    AIR_COOLED         | 12                            | true
    AIR_COOLED         | 13                            | false

    CHILLED            | -3                            | false
    CHILLED            | -2                            | true
    CHILLED            | 5                             | true
    CHILLED            | 6                             | true
    CHILLED            | 7                             | false

    FROZEN             | -21                           | false
    FROZEN             | -20                           | true
    FROZEN             | -10                           | true
    FROZEN             | -8                            | true
    FROZEN             | -7                            | false
  }

  @SuppressWarnings('GroovyPointlessBoolean')
  void "isStorageTemperatureAllowed method should work for null param as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam as CommodityType

    when:
    commodityType.isStorageTemperatureAllowed(null)

    then:
    thrown(AssertionError)

    where:
    commodityTypeParam << CommodityType.values()
  }

  void "isStorageTemperatureLimited method should work as expected"() {
    given:
    CommodityType commodityType = commodityTypeParam as CommodityType

    when:
    Boolean result = commodityType.isStorageTemperatureLimited()

    then:
    result == resultParam

    where:
    commodityTypeParam | resultParam
    DRY                | false
    AIR_COOLED         | true
    CHILLED            | true
    FROZEN             | true
  }
}
