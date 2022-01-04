/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.lib.boundary.api.application.exception

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
