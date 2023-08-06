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
package org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response

import groovy.transform.CompileStatic

/**
 * Defines possible kinds of violations that can happen during request processing and are reported as part of response.
 */
@CompileStatic
enum ViolationType {
  /**
   * Violation type corresponding to unsatisfied domain invariants.
   */
  DOMAIN,

  /**
   * Violation type corresponding to failed input data validation.
   */
  VALIDATION,

  /**
   * Corresponds to infrastructural violation that is detected and handled during processing of HTTP requests.
   * <p/>
   * For example, in web application built on top of Spring MVC, this violation can correspond to exceptions handled by {@code ResponseEntityExceptionHandler}.
   */
  INFRASTRUCTURE_WEB,

  /**
   * Corresponds to the violation that is not handled.
   * <p/>
   * Usually violations of this type result from bugs in application code (something like {@code NullPointerException}), or to the failing/unavailable infrastructure (something like timeouts while
   * trying to access remote service).
   */
  UNKNOWN
}
