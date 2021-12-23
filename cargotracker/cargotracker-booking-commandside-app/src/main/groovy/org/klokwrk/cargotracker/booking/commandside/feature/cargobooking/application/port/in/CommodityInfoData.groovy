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
class CommodityInfoData {
  /**
   * Commodity type defined as the value of {@link CommodityType} enum.
   * <p/>
   * Not {@code null}.
   */
  @NotNull(groups = [Level1])
  CommodityType commodityType

  /**
   * Commodity total weight in kilograms.
   * <p/>
   * Not {@code null} and must be 1 or greater.
   */
  @Min(value = 1L, groups = [Level2])
  @NotNull(groups = [Level1])
  Integer totalWeightInKilograms

  /**
   * The requested storage temperature in Celsius degree.
   * <p/>
   * Definite storage temperature validation is done by business logic, and it depends on the supported temperature range of the selected commodity type.
   * <p/>
   * Here we are just validating a sensible storage temperature range of [-30, 30] Celsius inclusively to avoid accepting completely unbounded integers. The range chosen here is equal to the
   * supported range of reefer containers.
   */
  @Max(value = 30L, groups = [Level2])
  @Min(value = -30L, groups = [Level2])
  Integer requestedStorageTemperatureInCelsius
}
