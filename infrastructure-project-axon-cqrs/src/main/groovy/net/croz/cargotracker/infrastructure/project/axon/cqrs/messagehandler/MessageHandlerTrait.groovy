package net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler

import groovy.transform.CompileStatic

/**
 * Base class for more specific handler traits.
 */
@CompileStatic
trait MessageHandlerTrait {
  /**
   * Non-stacktrace throw-away exception intended to be used as a (not interesting placeholder) cause of Axon's <code>Command/QueryExecutionException</code> when throwing much more important business
   * exceptions (propagated as details exception to the caller) from Axon's command/query handlers.
   */
  @SuppressWarnings("Indentation")
  static class ThrowAwayRuntimeException extends RuntimeException {
    ThrowAwayRuntimeException() {
      super(null, null, false, false)
    }
  }
}
