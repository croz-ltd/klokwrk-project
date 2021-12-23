package org.klokwrk.cargotracker.booking.domain.model.value

import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.CHILLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.AIR_COOLED
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.DRY
import static org.klokwrk.cargotracker.booking.domain.model.value.CommodityType.FROZEN
import static tech.units.indriya.quantity.Quantities.getQuantity
import static tech.units.indriya.unit.Units.CELSIUS
import static tech.units.indriya.unit.Units.KILOGRAM

class CommodityInfoSpecification extends Specification {
  static Quantity<Mass> oneKilogram = getQuantity(1, KILOGRAM)

  void "map constructor should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = new CommodityInfo(commodityType: commodityTypeParam, totalWeight: oneKilogram, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    commodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | getQuantity(25, CELSIUS)

    AIR_COOLED         | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(8, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)

    CHILLED            | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(3, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)

    FROZEN             | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)
  }

  void "map constructor should fail for null input params"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: totalWeightParam, requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    commodityTypeParam | totalWeightParam         | requestedStorageTemperatureParam
    null               | getQuantity(1, KILOGRAM) | getQuantity(1, CELSIUS)
    DRY                | null                     | getQuantity(1, CELSIUS)
  }

  void "map constructor should fail for invalid totalWeight param"() {
    when:
    new CommodityInfo(commodityType: DRY, totalWeight: getQuantity(totalWeightValueParam, KILOGRAM), requestedStorageTemperature: getQuantity(1, CELSIUS))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(totalWeight)]")

    where:
    totalWeightValueParam | _
    0                     | _
    -1                    | _
  }

  void "map constructor should fail for null requestedStorageTemperature when requestedStorageTemperature is required"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    thrown(AssertionError)

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    AIR_COOLED         | null
    CHILLED            | null
    FROZEN             | null
  }

  void "map constructor should fail for requestedStorageTemperature not in required range"() {
    when:
    new CommodityInfo(commodityType: commodityTypeParam, totalWeight: getQuantity(1, KILOGRAM), requestedStorageTemperature: requestedStorageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    commodityTypeParam | requestedStorageTemperatureParam | violationCodeKeyParam
    AIR_COOLED         | getQuantity(1, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED         | getQuantity(13, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED            | getQuantity(-3, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED            | getQuantity(7, CELSIUS)          | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN             | getQuantity(-21, CELSIUS)        | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN             | getQuantity(-7, CELSIUS)         | "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
  }

  void "create(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram, requestedStorageTemperatureParam)

    then:
    commodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null

    AIR_COOLED         | getQuantity(2, CELSIUS)
    AIR_COOLED         | getQuantity(7, CELSIUS)
    AIR_COOLED         | getQuantity(12, CELSIUS)

    CHILLED            | getQuantity(-2, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    CHILLED            | getQuantity(6, CELSIUS)

    FROZEN             | getQuantity(-20, CELSIUS)
    FROZEN             | getQuantity(-15, CELSIUS)
    FROZEN             | getQuantity(-8, CELSIUS)
  }

  void "create(CommodityType, Quantity, Quantity) factory method should acquire recommendedStorageTemperature when requestedStorageTemperature is not given"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram, null)

    then:
    commodityInfo.requestedStorageTemperature == commodityTypeParam.recommendedStorageTemperature

    where:
    commodityTypeParam | _
    DRY                | _
    AIR_COOLED         | _
    CHILLED            | _
    FROZEN             | _
  }

  void "create(CommodityType, Quantity, Quantity) factory method should skip requiring recommendedStorageTemperature for the combination of invalid input params"() {
    when:
    CommodityInfo.create(null, oneKilogram, null)

    then:
    thrown(AssertionError)
  }

  void "create(CommodityType, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, oneKilogram)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }

  void "create(CommodityType, Integer, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, 1, requestedStorageTemperatureParam)

    then:
    commodityInfo

    where:
    commodityTypeParam | requestedStorageTemperatureParam
    DRY                | null

    AIR_COOLED         | 2
    AIR_COOLED         | 8
    AIR_COOLED         | 12

    CHILLED            | -2
    CHILLED            | 3
    CHILLED            | 6

    FROZEN             | -20
    FROZEN             | -15
    FROZEN             | -8
  }

  void "create(CommodityType, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(commodityTypeParam, 1)

    then:
    commodityInfo
    commodityInfo.requestedStorageTemperature == recommendedStorageTemperatureParam

    where:
    commodityTypeParam | recommendedStorageTemperatureParam
    DRY                | null
    AIR_COOLED         | getQuantity(6, CELSIUS)
    CHILLED            | getQuantity(0, CELSIUS)
    FROZEN             | getQuantity(-12, CELSIUS)
  }
}
