package org.klokwrk.cargotracker.lib.boundary.api.exception

import groovy.transform.CompileStatic

/**
 * Runtime exception with identifier (usually UUID string).
 * <p/>
 * May be used in any scenario, but is primarily intended for remoting use cases when is sometimes desirable to have explicit exception identifier to be able to correlate duplicated  stacktraces
 * (in server and client logs).
 */
@CompileStatic
class IdentifiedRuntimeException extends RuntimeException {
  String exceptionId

  IdentifiedRuntimeException() {
    this(UUID.randomUUID().toString())
  }

  IdentifiedRuntimeException(String exceptionId) {
    this(exceptionId, null)
  }

  IdentifiedRuntimeException(String exceptionId, String message) {
    this(exceptionId, message, null)
  }

  IdentifiedRuntimeException(String exceptionId, String message, Throwable cause) {
    this(exceptionId, message, cause, true)
  }

  IdentifiedRuntimeException(String exceptionId, String message, Throwable cause, Boolean writableStackTrace) {
    super(message, cause, false, writableStackTrace)
    this.exceptionId = exceptionId
  }
}
