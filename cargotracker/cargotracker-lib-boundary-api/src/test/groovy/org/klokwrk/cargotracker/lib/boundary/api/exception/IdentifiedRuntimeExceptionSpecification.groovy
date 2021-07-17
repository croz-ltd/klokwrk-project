package org.klokwrk.cargotracker.lib.boundary.api.exception

import spock.lang.Specification

class IdentifiedRuntimeExceptionSpecification extends Specification {
  void "should be constructed as expected with default constructor"() {
    given:
    String someUuid = UUID.randomUUID()

    when:
    IdentifiedRuntimeException identifiedRuntimeException = new IdentifiedRuntimeException()

    then:
    identifiedRuntimeException.exceptionId.size() == someUuid.size()
    identifiedRuntimeException.message == null
    identifiedRuntimeException.stackTrace.size() > 0
  }

  void "should be constructed as expected with full constructor"() {
    given:
    String exceptionUuid = UUID.randomUUID()
    String exceptionMessage = "some exception message"
    RuntimeException exceptionCause = new RuntimeException("cause")

    when:
    IdentifiedRuntimeException identifiedRuntimeException = new IdentifiedRuntimeException(exceptionUuid, exceptionMessage, exceptionCause, writableStackTraceParam)

    then:
    identifiedRuntimeException.exceptionId == exceptionUuid
    identifiedRuntimeException.message == exceptionMessage
    identifiedRuntimeException.cause == exceptionCause

    if (writableStackTraceParam) {
      identifiedRuntimeException.stackTrace.size() > 0
    }
    else {
      identifiedRuntimeException.stackTrace.size() == 0
    }

    where:
    writableStackTraceParam | _
    true                    | _
    false                   | _
  }
}
