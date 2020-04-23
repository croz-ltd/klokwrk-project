package net.croz.cargotracker.api.open.shared.exceptional.exception

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.violation.ViolationInfo

@CompileStatic
class CommandException extends DomainException {
  CommandException() {
    this(ViolationInfo.UNKNOWN)
  }

  CommandException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.codeMessage)
  }

  CommandException(ViolationInfo violationInfo, String message) {
    this(violationInfo, message, false)
  }

  CommandException(ViolationInfo violationInfo, String message, Boolean writableStackTrace) {
    this(violationInfo, message, null, writableStackTrace)
  }

  CommandException(ViolationInfo violationInfo, String message, Throwable cause, Boolean writableStackTrace) {
    super(violationInfo, message, cause, writableStackTrace)
  }
}
