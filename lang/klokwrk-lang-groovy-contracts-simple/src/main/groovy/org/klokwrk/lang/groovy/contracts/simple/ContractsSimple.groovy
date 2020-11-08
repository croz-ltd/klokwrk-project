/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.contracts.simple

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext
import org.klokwrk.lang.groovy.contracts.base.ContractsBase

/**
 * Defines simple DBC (Design-by-Contracts) methods with enhanced messages containing textual representation of used boolean expressions.
 * <p/>
 * For example, the code fragment
 * <pre>
 * requireTrue(1 > 10)
 * </pre>
 * will result in {@code AssertionError} with the message
 * <pre>
 * Require violation detected - boolean condition is false - [condition: (1 > 10)]
 * </pre>
 * <p/>
 * Implementation uses Groovy macro methods to extract textual representation of tested boolean expressions which is very convenient to have in {@code AssertionError} messages.
 */
@SuppressWarnings("unused")
@CompileStatic
class ContractsSimple {
  static final String REQUIRE_TRUE_MESSAGE_DEFAULT = "Require violation detected - boolean condition is false"

  /**
   * DBC precondition check based on boolean expression.
   * <p/>
   * It adds convenient message containing textual representation of actually used boolean expression.
   * <p/>
   * At the implementation level, when this Groovy macro method is expanded and replaced, we end up with the method call to
   * {@link ContractsBase#requireTrueBase(java.lang.Boolean, java.lang.String)}.
   */
  @SuppressWarnings("UnusedMethodParameter")
  @Macro
  static Expression requireTrue(MacroContext macroContext, Expression expression) {
    String expressionText = expression.text
    StaticMethodCallExpression staticMethodCallExpression = GeneralUtils.callX(
        new ClassNode(ContractsBase), "requireTrueBase", GeneralUtils.args(expression, new ConstantExpression("$REQUIRE_TRUE_MESSAGE_DEFAULT - [condition: $expressionText]".toString()))
    )

    return staticMethodCallExpression
  }
}
