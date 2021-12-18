package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler
import org.klokwrk.lib.validation.group.Level1
import org.klokwrk.lib.validation.group.Level2

import javax.validation.GroupSequence
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * DTO encapsulating commodity info data pieces gathered from external ports/adapters.
 */
@GroupSequence([CommodityInfoData, Level1, Level2])
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
@CompileStatic
class CommodityInfoData {
  /**
   * Commodity type as defined by {@link CommodityType} enum.
   * <p/>
   * Not {@code null}.
   */
  @NotNull(groups = [Level1])
  CommodityType type

  /**
   * Commodity total weight in kilograms.
   * <p/>
   * Not {@code null} and must be 1 or greater.
   */
  @Min(value = 1L, groups = [Level2])
  @NotNull(groups = [Level1])
  Integer weightInKilograms

  /**
   * Storage temperature in Celsius degree.
   * <p/>
   * Some commodity types require specification of storage temperature. For such commodity types {@code storageTemperatureInCelsius} must not be {@code null}. For all other commodity types in has to
   * be {@code null}.
   * <p/>
   * When specified, the allowed range is between -35 and 35 Celsius inclusive. This is dictated by the maximum temperature range of the reefer container in which related commodity types will be
   * transported.
   */
  @Max(value = 35L, groups = [Level2])
  @Min(value = -35L, groups = [Level2])
  Integer storageTemperatureInCelsius
}
