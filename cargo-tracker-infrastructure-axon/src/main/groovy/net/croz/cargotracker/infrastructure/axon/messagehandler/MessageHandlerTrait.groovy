package net.croz.cargotracker.infrastructure.axon.messagehandler

import groovy.transform.CompileStatic

@CompileStatic
trait MessageHandlerTrait {
  static class ThrowAwayRuntimeException extends RuntimeException {
    ThrowAwayRuntimeException() {
      super(null, null, false, false)
    }
  }
}
