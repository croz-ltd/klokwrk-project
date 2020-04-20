package net.croz.cargotracker.api.open.shared.exceptional.exception

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.violation.ViolationInfo

@CompileStatic
class DomainException extends RuntimeException {
  ViolationInfo violationInfo

  DomainException() {
    this(ViolationInfo.UNKNOWN)
  }

  DomainException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.text)
  }

  DomainException(ViolationInfo violationInfo, String message) {
    this(violationInfo, message, false)
  }

  DomainException(ViolationInfo violationInfo, String message, Boolean writableStackTrace) {
    this(violationInfo, message, null, writableStackTrace)
  }

  DomainException(ViolationInfo violationInfo, String message, Throwable cause, Boolean writableStackTrace) {
    super(message ?: violationInfo.violationCode.text, cause, false, writableStackTrace)
    this.violationInfo = violationInfo
  }
}
