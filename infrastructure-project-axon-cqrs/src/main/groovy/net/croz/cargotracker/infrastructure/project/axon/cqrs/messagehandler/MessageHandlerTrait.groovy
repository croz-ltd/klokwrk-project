package net.croz.cargotracker.infrastructure.project.axon.cqrs.messagehandler

import groovy.transform.CompileStatic

@CompileStatic
trait MessageHandlerTrait {
  @SuppressWarnings("Indentation")
  static class ThrowAwayRuntimeException extends RuntimeException {
    ThrowAwayRuntimeException() {
      super(null, null, false, false)
    }
  }
}
