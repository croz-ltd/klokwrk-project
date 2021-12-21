package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.quantity.QuantityRange
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

import static org.hamcrest.Matchers.notNullValue

/**
 * Describes commodity carried by the cargo.
 */
@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class CommodityInfo implements PostMapConstructorCheckable {
  /**
   * Type of commodity.
   * <p/>
   * Only a single commodity type is allowed for the whole cargo.
   * <p/>
   * Must not be {@code null}.
   */
  CommodityType type

  /**
   * Total commodity weight.
   * <p/>
   * It might cause spreading commodities over multiple containers.
   * <p/>
   * Must not be {@code null}, and must be at least 1 kg or greater. Do note it does not have to be specified in kilograms.
   */
  Quantity<Mass> weight

  /**
   * Storage temperature that commodity requires during transportation.
   * <p/>
   * It might cause requiring different types of containers, i.e. reefer container.
   * <p/>
   * Whether it is required or not, depends on commodity type. For example, it must not be specified for {@code DRY} commodity type, but it must be specified for {@code CHILLED} commodity type.
   * Commodity type also determines allowed temperature range.
   * <p/>
   * When specified, storage temperature must not be outside of a range supported by reefer container: [-35, 35] Celsius inclusive.
   */
  Quantity<Temperature> storageTemperature

  static CommodityInfo create(CommodityType type, Quantity<Mass> weight, Quantity<Temperature> storageTemperature) {
    CommodityInfo commodityInfo = new CommodityInfo(type: type, weight: weight, storageTemperature: storageTemperature)
    return commodityInfo
  }

  static CommodityInfo create(CommodityType type, Integer weightInKilograms, Integer storageTemperatureInCelsius) {
    CommodityInfo commodityInfo = create(
        type,
        Quantities.getQuantity(weightInKilograms, Units.KILOGRAM),
        storageTemperatureInCelsius == null ? null : Quantities.getQuantity(storageTemperatureInCelsius, Units.CELSIUS)
    )

    return commodityInfo
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(type, notNullValue())
    requireMatch(weight, notNullValue())

    requireTrue(Quantities.getQuantity(1, Units.KILOGRAM).isLessThanOrEqualTo(weight))

    requireMissingStorageTemperatureForCertainCommodityTypes(storageTemperature, type)
    requireStorageTemperatureForCertainCommodityTypes(storageTemperature, type)
    requireStorageTemperatureInAllowedRange(storageTemperature, type)
  }

  private void requireMissingStorageTemperatureForCertainCommodityTypes(Quantity<Temperature> storageTemperature, CommodityType commodityType) {
    if (storageTemperature != null) {
      if (commodityType == CommodityType.DRY) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureNotAllowedForDryCommodityType"))
      }
    }
  }

  private void requireStorageTemperatureForCertainCommodityTypes(Quantity<Temperature> storageTemperature, CommodityType commodityType) {
    if (storageTemperature == null) {
      if (commodityType == CommodityType.AIR_COOLED) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureRequiredForAirCooledCommodityType"))
      }

      if (commodityType == CommodityType.CHILLED) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureRequiredForChilledCommodityType"))
      }

      if (commodityType == CommodityType.FROZEN) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureRequiredForFrozenCommodityType"))
      }
    }
  }

  private void requireStorageTemperatureInAllowedRange(Quantity<Temperature> storageTemperature, CommodityType commodityType) {
    if (storageTemperature != null) {
      // [-35, +35] celsius is a temperature range supported by reefer container
      QuantityRange<Temperature> storageTemperatureRange = QuantityRange.of(Quantities.getQuantity(-35, Units.CELSIUS), Quantities.getQuantity(35, Units.CELSIUS))
      if (!storageTemperatureRange.contains(storageTemperature)) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureNotInAllowedRange"))
      }

      Boolean isStorageTemperatureAllowed = commodityType.isStorageTemperatureAllowed(storageTemperature)
      if (commodityType == CommodityType.AIR_COOLED && !isStorageTemperatureAllowed) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureNotInAllowedRangeForAirCooledCommodityType"))
      }

      if (commodityType == CommodityType.CHILLED && !isStorageTemperatureAllowed) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureNotInAllowedRangeForChilledCommodityType"))
      }

      if (commodityType == CommodityType.FROZEN && !isStorageTemperatureAllowed) {
        throw new DomainException(ViolationInfo.createForBadRequestWithCustomCodeKey("commodityInfo.storageTemperatureNotInAllowedRangeForFrozenCommodityType"))
      }
    }
  }
}
