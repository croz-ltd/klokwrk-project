package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType

import javax.measure.Quantity
import javax.measure.quantity.Mass

/**
 * Domain policy service for calculating maximum allowed weight per container.
 * <p/>
 * {@link ContainerType} already specifies maximum allowed weight for that particular container type. However, business might want to reduce that weight further to avoid overweight containers.
 */
@CompileStatic
interface MaxAllowedWeightPerContainerPolicy {
  Quantity<Mass> maxAllowedWeightPerContainer(ContainerType containerType)
}
