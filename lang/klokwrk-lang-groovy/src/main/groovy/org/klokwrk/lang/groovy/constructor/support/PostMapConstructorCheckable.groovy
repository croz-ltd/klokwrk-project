package org.klokwrk.lang.groovy.constructor.support

import groovy.transform.CompileStatic

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
  }

  default Boolean postMapConstructorShouldThrowForEmptyConstructorArguments() {
    return false
  }

  void postMapConstructorCheck(Map<String, ?> constructorArguments)
}
