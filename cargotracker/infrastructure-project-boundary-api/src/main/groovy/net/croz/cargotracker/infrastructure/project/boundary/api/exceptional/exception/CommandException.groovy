package net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationInfo

/**
 * Intended to communicate non-fatal domain conditions that prevent successful fulfillment of domain command.
 */
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
