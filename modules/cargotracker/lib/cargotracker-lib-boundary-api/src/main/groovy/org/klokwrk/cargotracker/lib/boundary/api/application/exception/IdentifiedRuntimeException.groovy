/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
