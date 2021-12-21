package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.quantity.QuantityRange
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Temperature

/**
 * Enumerates types of commodity carried by the cargo.
 * <p/>
 * Available commodities are:
 * <ul>
 *   <li><b>DRY</b>: Type of commodity which does not require controlled temperature.</li>
 *   <li><b>AIR_COOLED</b>: Type of commodity which does require air cooled temperature environment. Allowed temperature range is [2, 12] Celsius inclusive.</li>
 *   <li><b>CHILLED</b>: Type of commodity which does require chilled temperature environment. Allowed temperature range is [-2, 6] Celsius inclusive.</li>
 *   <li><b>FROZEN</b>: Type of commodity which does require frozen temperature environment. Allowed temperature range is [-20, -8] Celsius inclusive.</li>
 * </ul>
 */
@CompileStatic
enum CommodityType {
  DRY(null),
  AIR_COOLED(Constants.AIR_COOLED_STORAGE_TEMPERATURE_RANGE),
  CHILLED(Constants.CHILLED_STORAGE_TEMPERATURE_RANGE),
  FROZEN(Constants.FROZEN_STORAGE_TEMPERATURE_RANGE)

  static class Constants {
    static final QuantityRange<Temperature> AIR_COOLED_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(2, Units.CELSIUS), Quantities.getQuantity(12, Units.CELSIUS))
    static final QuantityRange<Temperature> CHILLED_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(-2, Units.CELSIUS), Quantities.getQuantity(6, Units.CELSIUS))
    static final QuantityRange<Temperature> FROZEN_STORAGE_TEMPERATURE_RANGE = QuantityRange.of(Quantities.getQuantity(-20, Units.CELSIUS), Quantities.getQuantity(-8, Units.CELSIUS))
  }

  private final QuantityRange<Temperature> storageTemperatureRange

  private CommodityType(QuantityRange<Temperature> storageTemperatureRange) {
    this.storageTemperatureRange = storageTemperatureRange
  }

  Boolean isStorageTemperatureAllowed(Quantity<Temperature> storageTemperature) {
    if (this.storageTemperatureRange == null) {
      return true
    }

    return this.storageTemperatureRange.contains(storageTemperature)
  }
}
