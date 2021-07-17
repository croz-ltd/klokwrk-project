package org.klokwrk.cargotracker.lib.boundary.api.exception

import spock.lang.Specification

class RemoteHandlerExceptionSpecification extends Specification {
  void "should be constructed as expected with default constructor"() {
    given:
    String someUuid = UUID.randomUUID()

    when:
    RemoteHandlerException remoteHandlerException = new RemoteHandlerException()

    then:
    remoteHandlerException.exceptionId.size() == someUuid.size()
    remoteHandlerException.message == null
    remoteHandlerException.stackTrace.size() == 0
  }

  void "should be constructed as expected with full constructor"() {
    given:
    String exceptionUuid = UUID.randomUUID()
    String exceptionMessage = "some exception message"
    RuntimeException exceptionCause = new RuntimeException("cause")

    when:
    RemoteHandlerException remoteHandlerException = new RemoteHandlerException(exceptionUuid, exceptionMessage, exceptionCause, writableStackTraceParam)

    then:
    remoteHandlerException.exceptionId == exceptionUuid
    remoteHandlerException.message == exceptionMessage
    remoteHandlerException.cause == exceptionCause

    if (writableStackTraceParam) {
      remoteHandlerException.stackTrace.size() > 0
    }
    else {
      remoteHandlerException.stackTrace.size() == 0
    }

    where:
    writableStackTraceParam | _
    true                    | _
    false                   | _
  }
}
