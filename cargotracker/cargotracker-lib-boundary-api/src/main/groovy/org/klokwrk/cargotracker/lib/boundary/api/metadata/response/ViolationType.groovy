package org.klokwrk.cargotracker.lib.boundary.api.metadata.response

import groovy.transform.CompileStatic

/**
 * Defines possible kinds of violations that can happen during request processing and are reported as part of response.
 */
@CompileStatic
enum ViolationType {
  /**
   * Violation type corresponding to unsatisfied domain invariants.
   */
  DOMAIN,

  /**
   * Violation type corresponding to failed input data validation.
   */
  VALIDATION,

  /**
   * Corresponds to any other (except UNKNOWN) type of violation that is detected and handled.
   * <p/>
   * For example, in web application built on top of Spring MVC, this violation can correspond to exceptions handled by {@code ResponseEntityExceptionHandler}.
   */
  OTHER,

  /**
   * Corresponds to the violation that is not handled.
   * <p/>
   * Usually violations of this type result from bugs in application code (something like {@code NullPointerException}), or to the failing/unavailable infrastructure (something like timeouts while
   * trying to access remote service).
   */
  UNKNOWN
}
