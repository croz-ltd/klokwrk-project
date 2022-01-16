package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerDimensionType

/**
 * Domain service for high-level creation of {@link Commodity} instances.
 */
@CompileStatic
interface CommodityCreatorService {
  /**
   * Creates {@link Commodity} instance from provided {@link ContainerDimensionType} and {@link CommodityInfo}.
   */
  Commodity from(ContainerDimensionType containerDimensionType, CommodityInfo commodityInfo)
}
