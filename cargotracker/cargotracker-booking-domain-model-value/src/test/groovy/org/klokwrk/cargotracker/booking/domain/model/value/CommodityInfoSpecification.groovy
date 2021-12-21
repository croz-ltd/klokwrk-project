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

  void "COMMODITY_TYPE_TO_CONTAINER_ISO_TYPE_MAP constant should be of the right size"() {
    expect:
    CommodityInfo.COMMODITY_TYPE_TO_CONTAINER_ISO_TYPE_MAP.size() == CommodityType.values().size()
  }

  void "map constructor should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = new CommodityInfo(type: typeParam, weight: oneKilogram, storageTemperature: storageTemperatureParam)

    then:
    commodityInfo

    where:
    typeParam  | storageTemperatureParam
    DRY        | null

    AIR_COOLED | getQuantity(2, CELSIUS)
    AIR_COOLED | getQuantity(8, CELSIUS)
    AIR_COOLED | getQuantity(12, CELSIUS)

    CHILLED    | getQuantity(-2, CELSIUS)
    CHILLED    | getQuantity(3, CELSIUS)
    CHILLED    | getQuantity(6, CELSIUS)

    FROZEN     | getQuantity(-20, CELSIUS)
    FROZEN     | getQuantity(-15, CELSIUS)
    FROZEN     | getQuantity(-8, CELSIUS)
  }

  void "map constructor should fail for null input params"() {
    when:
    new CommodityInfo(type: typeParam, weight: weightParam)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("notNullValue")

    where:
    typeParam | weightParam
    null      | getQuantity(1, KILOGRAM)
    DRY       | null
  }

  void "map constructor should fail for invalid weight param"() {
    when:
    new CommodityInfo(type: DRY, weight: getQuantity(weightValueParam, KILOGRAM))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("boolean condition is false - [condition: Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(weight)]")

    where:
    weightValueParam | _
    0                | _
    -1               | _
  }

  void "map constructor should fail for invalid storage temperature param"() {
    when:
    new CommodityInfo(type: typeParam, weight: getQuantity(1, KILOGRAM), storageTemperature: storageTemperatureParam)

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeKey == violationCodeKeyParam

    where:
    typeParam  | storageTemperatureParam   | violationCodeKeyParam
    DRY        | getQuantity(10, CELSIUS)  | "commodityInfo.storageTemperatureNotAllowedForDryCommodityType"

    AIR_COOLED | null                      | "commodityInfo.storageTemperatureRequiredForAirCooledCommodityType"
    AIR_COOLED | getQuantity(-36, CELSIUS) | "commodityInfo.storageTemperatureNotInAllowedRange"
    AIR_COOLED | getQuantity(36, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRange"
    AIR_COOLED | getQuantity(1, CELSIUS)   | "commodityInfo.storageTemperatureNotInAllowedRangeForAirCooledCommodityType"
    AIR_COOLED | getQuantity(13, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRangeForAirCooledCommodityType"

    CHILLED    | null                      | "commodityInfo.storageTemperatureRequiredForChilledCommodityType"
    CHILLED    | getQuantity(-36, CELSIUS) | "commodityInfo.storageTemperatureNotInAllowedRange"
    CHILLED    | getQuantity(36, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRange"
    CHILLED    | getQuantity(-3, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRangeForChilledCommodityType"
    CHILLED    | getQuantity(7, CELSIUS)   | "commodityInfo.storageTemperatureNotInAllowedRangeForChilledCommodityType"

    FROZEN     | null                      | "commodityInfo.storageTemperatureRequiredForFrozenCommodityType"
    FROZEN     | getQuantity(-36, CELSIUS) | "commodityInfo.storageTemperatureNotInAllowedRange"
    FROZEN     | getQuantity(36, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRange"
    FROZEN     | getQuantity(-21, CELSIUS) | "commodityInfo.storageTemperatureNotInAllowedRangeForFrozenCommodityType"
    FROZEN     | getQuantity(-7, CELSIUS)  | "commodityInfo.storageTemperatureNotInAllowedRangeForFrozenCommodityType"
  }

  void "create(CommodityType, Quantity, Quantity) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(typeParam, oneKilogram, storageTemperatureParam)

    then:
    commodityInfo

    where:
    typeParam  | storageTemperatureParam
    DRY        | null

    AIR_COOLED | getQuantity(2, CELSIUS)
    AIR_COOLED | getQuantity(7, CELSIUS)
    AIR_COOLED | getQuantity(12, CELSIUS)

    CHILLED    | getQuantity(-2, CELSIUS)
    CHILLED    | getQuantity(0, CELSIUS)
    CHILLED    | getQuantity(6, CELSIUS)

    FROZEN     | getQuantity(-20, CELSIUS)
    FROZEN     | getQuantity(-15, CELSIUS)
    FROZEN     | getQuantity(-8, CELSIUS)
  }

  void "create(CommodityType, Integer, Integer) factory method should work for correct input params"() {
    when:
    CommodityInfo commodityInfo = CommodityInfo.create(typeParam, 1, storageTemperatureParam)

    then:
    commodityInfo

    where:
    typeParam  | storageTemperatureParam
    DRY        | null

    AIR_COOLED | 2
    AIR_COOLED | 8
    AIR_COOLED | 12

    CHILLED    | -2
    CHILLED    | 3
    CHILLED    | 6

    FROZEN     | -20
    FROZEN     | -15
    FROZEN     | -8
  }
}
