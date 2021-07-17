package org.klokwrk.cargotracker.lib.boundary.api.exception

import groovy.transform.CompileStatic

/**
 * Identifiable (command and query) handler exception with empty stacktrace by default.
 * <p/>
 * This exception is intended to be used from Axon command and query handlers as details DTO object. It should be used primarily in scenarios for describing unexpected exceptions (for example,
 * NullPointerException). For anticipated business exceptions, one should use DomainException hierarchy. For the usage example, take a look at {@code CommandHandlerExceptionInterceptor} or
 * {@code QueryHandlerExceptionInterceptor}
 * <p/>
 * Note the name similarity with Axon class - {@code org.axonframework.messaging.RemoteHandlingException}. Do not confuse the two.
 *
 * @see IdentifiedRuntimeException
 * @see DomainException
 * @see CommandException
 * @see QueryException
 */
@CompileStatic
class RemoteHandlerException extends IdentifiedRuntimeException {
  String exceptionMessage

  RemoteHandlerException() {
    this(UUID.randomUUID().toString())
  }

  RemoteHandlerException(String exceptionId) {
    this(exceptionId, null)
  }

  RemoteHandlerException(String exceptionId, String message) {
    this(exceptionId, message, null)
  }

  RemoteHandlerException(String exceptionId, String message, Throwable cause) {
    this(exceptionId, message, cause, false)
  }

  RemoteHandlerException(String exceptionId, String message, Throwable cause, boolean writableStackTrace) {
    super(exceptionId, message, cause, writableStackTrace)
    this.exceptionMessage = message
  }

  /**
   * Returns preserved exceptionMessage property instead of original message.
   * <p/>
   * Main reason for this is that, for some unknown reason, exception message field is lost during serialization over Axon Server. By adding additional exceptionMessage property, and overriding
   * getMessage() method, we can overcome this.
   */
  @Override
  String getMessage() {
    return this.exceptionMessage
  }
}
