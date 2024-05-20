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
package org.klokwrk.lib.xlang.groovy.base.contracts

import groovy.transform.CompileStatic

/**
 * Defines base DBC (Design-by-Contracts) methods.
 * <p/>
 * Does not introduce any dependencies or Groovy AST magic. Methods are intended to be used directly. This is suitable when used from the libraries at the lowest language extension abstraction level
 * since at that level we do not want to have any additional dependencies that are not absolutely necessary.
 * <p/>
 * At the abstraction levels that is higher than the lowest language extension abstraction level, there are other DBC modules/classes which might be more convenient and suitable, but usually
 * introduce additional dependencies. These higher level DBC modules/classes will usually delegate its calls to this base DBC module/class.
 */
@CompileStatic
class ContractsBase {
  static final String REQUIRE_TRUE_MESSAGE_DEFAULT = "Require violation detected - boolean condition is false"

  /**
   * DBC precondition check based on boolean expression and optional message.
   * <p/>
   * When condition evaluates to {@code false}, throws {@link AssertionError}.
   */
  static void requireTrueBase(boolean condition, String message = "$REQUIRE_TRUE_MESSAGE_DEFAULT.") {
    if (!condition) {
      String myMessage = message?.trim() ?: "$REQUIRE_TRUE_MESSAGE_DEFAULT."
      throw new AssertionError(myMessage as Object)
    }
  }
}
