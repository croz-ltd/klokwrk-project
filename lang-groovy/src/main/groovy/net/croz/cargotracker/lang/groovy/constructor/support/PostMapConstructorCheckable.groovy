package net.croz.cargotracker.lang.groovy.constructor.support

import groovy.transform.CompileStatic

/**
 * Defines protocol and simplifies implementation for common validation checking when <code>&#64;MapConstructor</code> is used with <code>post</code> attribute.
 * <p/>
 * It is intended to be used as an mean for verifying if object is in consistent state after creation. If not, <code>postMapConstructorCheck()</code> implementation should throw
 * <code>IllegalArgumentException</code> or <code>AssertionError</code>.
 * <p/>
 * If supplied <code>constructorArguments</code> map is empty, <code>postMapConstructorCheckProtocol()</code> immediately returns. Normally, it would be better to throw an exception, but many
 * libraries require availability of no-args constructor (i.e. jackson). There is an option to change this behavior by overriding <code>postMapConstructorShouldThrowForEmptyConstructorArguments()</code>.
 * After completing checking of <code>constructorArguments</code>, <code>postMapConstructorCheckProtocol()</code> calls <code>postMapConstructorCheck()</code> implementation.
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
 */
@CompileStatic
interface PostMapConstructorCheckable {
  default void postMapConstructorCheckProtocol(Map<String, ?> constructorArguments) {
    if (!constructorArguments) {
      if (postMapConstructorShouldThrowForEmptyConstructorArguments()) {
        throw new IllegalArgumentException("Map constructor's parameter map is empty. Cannot continue.")
      }

      return
    }

    try {
      postMapConstructorCheck(constructorArguments)
    }
    catch (AssertionError ae) {
      throw new IllegalArgumentException("\n${ ae.message }", ae)
    }
  }

  default Boolean postMapConstructorShouldThrowForEmptyConstructorArguments() {
    return false
  }

  void postMapConstructorCheck(Map<String, ?> constructorArguments)
}
