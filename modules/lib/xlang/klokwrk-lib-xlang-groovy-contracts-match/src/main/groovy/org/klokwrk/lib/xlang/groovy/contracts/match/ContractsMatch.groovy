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
package org.klokwrk.lib.xlang.groovy.contracts.match

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext

/**
 * Defines Hamcrest matcher based DBC (Design-by-Contracts) methods with enhanced messages containing textual representation of checked item and used Hamcrest matcher.
 * <p/>
 * For example, the code fragment
 * <pre>
 * requireMatch("123".trim(), is(emptyString()))
 * </pre>
 * will result in {@code AssertionError} with the message
 * <pre>
 * Require violation detected - matcher does not match - [item: 123.trim(), expected: is(emptyString()), actual: 123]
 * </pre>
 * <p/>
 * Implementation uses Groovy macro methods to extract textual representation of tested item and used matcher, which is very convenient to have in {@code AssertionError} messages.
 */
@SuppressWarnings(["unused", "CodeNarc.UnusedMethodParameter", "CodeNarc.DuplicateStringLiteral"])
@CompileStatic
class ContractsMatch {
  /**
   * Groovy macro method that implements DBC precondition check based on Hamcrest matchers.
   * <p/>
   * It adds convenient messages containing textual representation checked item and used matcher.
   * <p/>
   * At the implementation level, when this Groovy macro method is expanded and replaced, we end up with the method call to
   * {@link ContractsMatchBase#requireMatchBase(java.lang.Object, org.hamcrest.Matcher, java.lang.String, java.lang.String)}.
   */
  @Macro
  static Expression requireMatch(MacroContext macroContext, Expression itemExpression, Expression matcherExpression) {
    String itemDescription = itemExpression.text
    String matcherDescription = matcherExpression.text.replaceAll("this.", "")

    StaticMethodCallExpression staticMethodCallExpression = GeneralUtils.callX(
        new ClassNode(ContractsMatchBase),
        "requireMatchBase",
        GeneralUtils.args(itemExpression, matcherExpression, new ConstantExpression(itemDescription), new ConstantExpression(matcherDescription))
    )

    return staticMethodCallExpression
  }

  @Macro
  static Expression requireMatchWhenNotNull(MacroContext macroContext, Expression itemExpression, Expression matcherExpression) {
    String itemDescription = itemExpression.text
    String matcherDescription = matcherExpression.text.replaceAll("this.", "")

    StaticMethodCallExpression staticMethodCallExpression = GeneralUtils.callX(
        new ClassNode(ContractsMatchBase),
        "requireMatchWhenNotNullBase",
        GeneralUtils.args(itemExpression, matcherExpression, new ConstantExpression(itemDescription), new ConstantExpression(matcherDescription))
    )

    return staticMethodCallExpression
  }
}
