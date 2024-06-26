/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.lib.boundary.api.domain.exception

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo

/**
 * Intended to communicate non-fatal domain conditions that prevent successful fulfillment of the requested operation.
 * <p/>
 * Domain violation conditions are expressed via contained {@link ViolationInfo} structure.
 * <p/>
 * By default (meaning, when using simpler constructors), stack-trace is not created. The primary reason is that {@code DomainException}, and all its subclasses, are used as alternative response
 * value from the domain. They should represent benign violations like validation errors or not-found scenarios for queries. Serious error or fatal conditions should not be communicated via
 * {@code DomainException} hierarchy.
 * <p/>
 * In addition, the lack of stack-trace is also beneficial for technical reasons since our domain implementations (aggregates and query handlers) are communicating remotely with domain facade
 * services at the domain boundary. However, there should be means (and there are) to produce relevant stack traces, at least for development. Implementation of such development-time helpers can be
 * examined in {@code CommandHandlerExceptionInterceptor} and {@code QueryHandlerExceptionInterceptor} classes.
 *
 * @see ViolationInfo
 */
@CompileStatic
class DomainException extends RuntimeException {

  /**
   * Data structure describing the reason for the exception.
   */
  ViolationInfo violationInfo

  // Note: Need to have a custom message property here for correct handling Jackson deserialization of exception messages. For creating exceptions during deserialization, Jackson requires default
  //       constructor, or constructor with single String argument, or explicit usage of JsonCreator annotation (for more details, take a look at Jackson's
  //       ThrowableDeserializer.deserializeFromObject(JsonParser, DeserializationContext) method).
  //
  //       Since I don't want to use JsonCreator annotation, and single String argument constructor does not suits me, I ended up with custom message property. Do note that message property
  //       overrides getMessage() method from Throwable class.
  //
  //       After deserialization, Throwable.detailsMessage will always contain ViolationCode.UNKNOWN.codeMessage since Jackson will use default constructor for deserializing DomainException.
  String message

  DomainException() {
    this(ViolationInfo.UNKNOWN)
  }

  DomainException(ViolationInfo violationInfo) {
    this(violationInfo, violationInfo.violationCode.codeMessage)
  }

  DomainException(ViolationInfo violationInfo, String message) {
    this(violationInfo, message, false)
  }

  DomainException(ViolationInfo violationInfo, String message, Boolean writableStackTrace) {
    this(violationInfo, message, null, writableStackTrace)
  }

  DomainException(ViolationInfo violationInfo, String message, Throwable cause, Boolean writableStackTrace) {
    super(message?.trim() ?: violationInfo.violationCode.codeMessage, cause, false, writableStackTrace)

    this.violationInfo = violationInfo
    this.message = message?.trim() ?: violationInfo.violationCode.codeMessage
  }
}
