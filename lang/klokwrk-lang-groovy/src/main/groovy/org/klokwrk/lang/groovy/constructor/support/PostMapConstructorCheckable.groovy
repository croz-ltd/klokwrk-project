/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.constructor.support

import groovy.transform.CompileStatic
import groovy.transform.Generated

/**
 * Defines protocol and simplifies implementation for common precondition checking (in Design-By-Contract style) when {@code @MapConstructor} is used with {@code post} attribute.
 * <p/>
 * It is intended to be used as an mean for checking if object is in consistent state after creation, which is very convenient for immutable-like objects. If not, {@code postMapConstructorCheck()}
 * implementation should throw {@code AssertionError}.
 * <p/>
 * If supplied {@code constructorArguments} map is empty, {@code postMapConstructorCheckProtocol()} immediately returns. Normally, it would be better to throw an exception, but many libraries require
 * availability of no-args constructor (i.e. Jackson or JPA). There is an option to change this behavior by overriding {@code postMapConstructorShouldThrowForEmptyConstructorArguments()}. After
 * completing checking of {@code constructorArguments}, {@code postMapConstructorCheckProtocol()} calls {@code postMapConstructorCheck()} implementation.
 * <p/>
 * Example usage:
 * <pre>
 *   &#64;MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
 *   class UnLoCode implements PostMapConstructorCheckable {
 *     String code
 *
 *     &#64;Override
 *     void postMapConstructorCheck(Map<String, ?> constructorArguments) {
 *       assert code
 *       assert code.isBlank() == false
 *     }
 *   }
 * </pre>
 * <p/>
 * When used on abstraction levels higher than language extensions and reusable generic libraries, it is more idiomatic to use global {@code requireTrue} or {@code requireMatch} methods provided
 * by {@code klokwrk-lang-groovy-contracts-simple} or by {@code klokwrk-lang-groovy-contracts-match} modules like in the following example:
 * <pre>
 *   &#64;MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
 *   class UnLoCode implements PostMapConstructorCheckable {
 *     String code
 *
 *     &#64;Override
 *     void postMapConstructorCheck(Map<String, ?> constructorArguments) {
 *       requireMatch(code, not(blankOrNullString()))
 *     }
 *   }
 * </pre>
 * <p/>
 * When consistency checks of immutable objects are finished, there is often a need to do additional post-constructor processing. For example, at this point, we might want to calculate values of all
 * derived properties. For that post-constructor processing purpose, one may override and use {@link PostMapConstructorCheckable#postMapConstructorPostCheckProcess(java.util.Map)} method. Default
 * implementation of <code>postMapConstructorProcess(Map constructorArguments)</code> does nothing.
 */
@CompileStatic
interface PostMapConstructorCheckable {
  default void postMapConstructorCheckProtocol(Map<String, ?> constructorArguments) {
    if (!constructorArguments) {
      if (postMapConstructorShouldThrowForEmptyConstructorArguments()) {
        throw new AssertionError("Map constructor's parameter map is empty. Cannot continue." as Object)
      }

      return
    }

    postMapConstructorCheck(constructorArguments)
    postMapConstructorPostCheckProcess(constructorArguments)
  }

  /**
   * Default implementation returns {@code false}.
   * <p/>
   * Method is annotated with {@link Generated} to avoid its appearance in coverage reports as it is not usually overridden for reasons stated in class' Groovydoc.
   */
  @Generated
  default Boolean postMapConstructorShouldThrowForEmptyConstructorArguments() {
    return false
  }

  void postMapConstructorCheck(Map<String, ?> constructorArguments)

  @SuppressWarnings(["CodeNarc.EmptyMethod", "CodeNarc.UnusedMethodParameter"])
  default void postMapConstructorPostCheckProcess(Map<String, ?> constructorArguments) {
  }
}
