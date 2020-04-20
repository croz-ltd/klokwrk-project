package net.croz.cargotracker.api.open.shared.exceptional.exception

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.violation.ViolationInfo

@CompileStatic
class QueryException extends DomainException {
  QueryException() {
    this(ViolationInfo.UNKNOWN)
  }

  QueryException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.text)
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
