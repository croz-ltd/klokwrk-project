package net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationInfo

/**
 * Intended to communicate non-fatal domain conditions that prevent successful fulfillment of domain query.
 */
@CompileStatic
class QueryException extends DomainException {
  QueryException() {
    this(ViolationInfo.UNKNOWN)
  }

  QueryException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.codeMessage)
  }

  QueryException(ViolationInfo violationInfo, String message) {
    this(violationInfo, message, false)
  }

  QueryException(ViolationInfo violationInfo, String message, Boolean writableStackTrace) {
    this(violationInfo, message, null, writableStackTrace)
  }

  QueryException(ViolationInfo violationInfo, String message, Throwable cause, Boolean writableStackTrace) {
    super(violationInfo, message, cause, writableStackTrace)
  }
}
