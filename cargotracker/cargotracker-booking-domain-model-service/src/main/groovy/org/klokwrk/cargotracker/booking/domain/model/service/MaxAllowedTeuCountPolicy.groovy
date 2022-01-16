package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic

/**
 * Domain policy service for prescribing maximum allowed number of container TEU units.
 * <p/>
 * Intended to be used at the aggregate level to limit TEU units per booking offer.
 */
@CompileStatic
interface MaxAllowedTeuCountPolicy {
  Boolean isTeuCountAllowed(BigDecimal teuCount)
}
