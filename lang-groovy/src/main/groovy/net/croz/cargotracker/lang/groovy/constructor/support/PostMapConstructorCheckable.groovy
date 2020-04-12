package net.croz.cargotracker.lang.groovy.constructor.support

/**
 * Defines protocol and simplifies implementation for common validation checking when <code>&#64;MapConstructor</code> is used with <code>post</code> attribute.
 * <p/>
 * It is intended to be used as an mean for verifying if object is in consistent state after creation. If not, <code>postMapConstructorCheck()</code> implementation should throw
 * <code>IllegalArgumentException</code> or <code>AssertionError</code>.
 * <p/>
 * If supplied <code>constructorArguments</code> map is empty, <code>postMapConstructorCheckProtocol()</code> immediately returns. Otherwise, it calls <code>postMapConstructorCheck()</code>
 * implementation provided by the implementor of this interface.
 * <p/>
 * Example usage:
 * <pre>
 *   &#64;MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
 *   &#64;Override
 *   class UnLoCode implements PostMapConstructorCheckable {
 *     String code
 *
 *     void postMapConstructorCheck(Map<String, ?> constructorArguments) {
 *       assert code
 *       assert code.isBlank() == false
 *     }
 *   }
 * </pre>
 */
interface PostMapConstructorCheckable {
  default void postMapConstructorCheckProtocol(Map<String, ?> constructorArguments) {
    if (!constructorArguments) {
      return
    }

    try {
      postMapConstructorCheck(constructorArguments)
    }
    catch (AssertionError ae) {
      throw new IllegalArgumentException("\n${ ae.getMessage() }", ae)
    }
  }

  void postMapConstructorCheck(Map<String, ?> constructorArguments)
}
