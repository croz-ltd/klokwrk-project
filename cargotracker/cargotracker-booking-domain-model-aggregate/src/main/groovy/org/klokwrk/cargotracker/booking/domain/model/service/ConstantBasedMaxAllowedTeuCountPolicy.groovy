package org.klokwrk.cargotracker.booking.domain.model.service

import groovy.transform.CompileStatic

import static org.hamcrest.Matchers.notNullValue

/**
 * {@link MaxAllowedTeuCountPolicy} implementation that limits container TEU count based on provided constant.
 */
@CompileStatic
class ConstantBasedMaxAllowedTeuCountPolicy implements MaxAllowedTeuCountPolicy {
  BigDecimal maxAllowedTeuCount

  /**
   * Constructor.
   * <p/>
   * Parameter {@code maxAllowedTeuCount} must not be {@code null}.<br/>
   */
  ConstantBasedMaxAllowedTeuCountPolicy(BigDecimal maxAllowedTeuCount) {
    requireMatch(maxAllowedTeuCount, notNullValue())

    this.maxAllowedTeuCount = maxAllowedTeuCount
  }

  @Override
  Boolean isTeuCountAllowed(BigDecimal teuCountToCheck) {
    if (teuCountToCheck <= maxAllowedTeuCount) {
      return true
    }

    return false
  }
}
