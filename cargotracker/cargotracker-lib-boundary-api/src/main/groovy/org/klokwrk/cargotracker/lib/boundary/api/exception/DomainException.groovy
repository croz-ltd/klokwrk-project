package org.klokwrk.cargotracker.lib.boundary.api.exception

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo

/**
 * Intended to communicate non-fatal domain conditions that prevent successful fulfillment of the requested operation.
 * <p/>
 * Domain violation conditions are expressed via contained {@link ViolationInfo} structure.
 * <p/>
 * By default (meaning, when using simpler constructors), stack-trace is not created. The primary reason is that <code>DomainException</code>, and all its subclasses, are used as alternative response
 * value from the domain. They should represent benign violations like validation errors or not-found scenarios for queries. Serious error or fatal conditions should not be communicated via
 * <code>DomainException</code> hierarchy.
 * <p/>
 * In addition, the lack of stack-trace is also beneficial for technical reasons since our domain implementations (aggregates and query handlers) are communicating remotely with domain facade
 * services at the domain boundary. However, there should be means (and there are) to produce relevant stack traces, at least for development. Implementation of such development-time helpers can be
 * examined in <code>CommandHandlerTrait</code> and <code>QueryHandlerTrait</code> classes.
 *
 * @see ViolationInfo
 */
@CompileStatic
class DomainException extends RuntimeException {

  /**
   * Data structure describing the reason for the exception.
   */
  ViolationInfo violationInfo

  DomainException() {
    this(ViolationInfo.UNKNOWN)
  }

  DomainException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.codeMessage)
  }

  DomainException(ViolationInfo violationInfo, String message) {
    this(violationInfo, message, false)
  }

  DomainException(ViolationInfo violationInfo, String message, Boolean writableStackTrace) {
    this(violationInfo, message, null, writableStackTrace)
  }

  DomainException(ViolationInfo violationInfo, String message, Throwable cause, Boolean writableStackTrace) {
    super(message ?: violationInfo.violationCode.codeMessage, cause, false, writableStackTrace)
    this.violationInfo = violationInfo
  }
}
