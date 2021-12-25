package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

import static org.hamcrest.Matchers.notNullValue

/**
 * Describes commodity characteristics at the cargo booking level.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class CommodityInfo implements PostMapConstructorCheckable {
  /**
   * Type of commodity.
   * <p/>
   * Must not be {@code null}.
   */
  CommodityType commodityType

  /**
   * Total commodity weight.
   * <p/>
   * This weight might exceed what a single container can carry (in that case, multiple containers will be allocated for this commodity info).
   * <p/>
   * Must not be {@code null}, and must be at least 1 kg or greater. Do note it does not have to be specified in kilograms.
   */
  Quantity<Mass> totalWeight

  /**
   * Requested storage temperature for a commodity.
   * <p/>
   * Whether it is required or not depends on the commodity type. If required, it must be inside of temperature range boundaries of the corresponding commodity type.
   * <p/>
   * When using factory {@code create()} methods, if not provided, the requested storage temperature is populated from the recommended storage temperature of the corresponding commodity type.
   */
  Quantity<Temperature> requestedStorageTemperature

  static CommodityInfo create(CommodityType commodityType, Quantity<Mass> weight) {
    CommodityInfo commodityInfo = create(commodityType, weight, null)
    return commodityInfo
  }

  /**
   * The main factory method for creating {@code CommodityInfo} instance (all other {@code create()} factory methods delegate to this one).
   * <p/>
   * The only optional parameter (can be provided as {@code null}) is {@code requestedStorageTemperature}. When {@code null}, the actual {code requestedStorageTemperature} of the instance is set to
   * the {@code recommendedStorageTemperature} of provided {@code commodityType}. Note that the {@code recommendedStorageTemperature} of {@code commodityType} can be {@code null}.
   */
  static CommodityInfo create(CommodityType commodityType, Quantity<Mass> weight, Quantity<Temperature> requestedStorageTemperature) {
    Quantity<Temperature> requestedStorageTemperatureToUse = requestedStorageTemperature
    if (requestedStorageTemperature == null && commodityType != null) {
      requestedStorageTemperatureToUse = commodityType.recommendedStorageTemperature
    }

    CommodityInfo commodityInfo = new CommodityInfo(commodityType: commodityType, totalWeight: weight, requestedStorageTemperature: requestedStorageTemperatureToUse)
    return commodityInfo
  }

  static CommodityInfo create(CommodityType commodityType, Integer weightInKilograms) {
    CommodityInfo commodityInfo = create(commodityType, weightInKilograms, null)
    return commodityInfo
  }

  static CommodityInfo create(CommodityType commodityType, Integer weightInKilograms, Integer requestedStorageTemperatureInCelsius) {
    CommodityInfo commodityInfo = create(
        commodityType,
        Quantities.getQuantity(weightInKilograms, Units.KILOGRAM),
        requestedStorageTemperatureInCelsius == null ? null : Quantities.getQuantity(requestedStorageTemperatureInCelsius, Units.CELSIUS)
    )

    return commodityInfo
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(commodityType, notNullValue())
    requireMatch(totalWeight, notNullValue())

    requireTrue(Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(totalWeight))
    requireTrue(isRequestedStorageTemperatureAvailableWhenNeeded(requestedStorageTemperature, commodityType))

    requireRequestedStorageTemperatureInAllowedRange(requestedStorageTemperature, commodityType)
  }

  private Boolean isRequestedStorageTemperatureAvailableWhenNeeded(Quantity<Temperature> requestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.containerFeaturesType.isContainerTemperatureControlled() && requestedStorageTemperature == null) {
      return false
    }

    return true
  }

  private void requireRequestedStorageTemperatureInAllowedRange(Quantity<Temperature> requestedStorageTemperature, CommodityType commodityType) {
    if (commodityType.isStorageTemperatureLimited() && (!commodityType.isStorageTemperatureAllowed(requestedStorageTemperature))) {
      String messageKey
      switch (commodityType) {
        case CommodityType.AIR_COOLED:
          messageKey = "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForAirCooledCommodityType"
          break
        case CommodityType.CHILLED:
          messageKey = "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForChilledCommodityType"
          break
        case CommodityType.FROZEN:
          messageKey = "commodityInfo.requestedStorageTemperatureNotInAllowedRangeForFrozenCommodityType"
          break
        default:
          // As we are switching over enum values, just make sure that we are not missing some of them.
          throw new AssertionError("Unexpected CommodityType value: [value: ${ commodityType.name() }]", null)
      }

      throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey(messageKey))
    }
  }
}
