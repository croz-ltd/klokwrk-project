package org.klokwrk.lang.groovy.contracts.match

import groovy.transform.CompileStatic
import org.hamcrest.Matcher
import org.hamcrest.StringDescription

/**
 * Defines base DBC (Design-by-Contracts) methods that use Hamcrest matchers for expressing contracts.
 * <p/>
 * Methods defined here can be used by direct method calls. However, it is more convenient to use {@code requireMatch} alternatives implemented in {@code ContractsMatch} as Groovy macro methods.
 * Under the hood, these Groovy macro methods delegate to base methods defined here.
 */
@CompileStatic
class ContractsMatchBase {
  static final String REQUIRE_MATCH_MESSAGE_DEFAULT = "Require violation detected - matcher does not match"

  /**
   * DBC precondition check based on Hamcrest matcher.
   * </p>
   * When mismatch occurs, {@link AssertionError} is thrown.
   *
   * @param item Object that is checked.
   * @param matcher Hamcrest matcher expressing precondition.
   * @param itemDescription Optional description of the item.
   * @param matcherDescription Optional description of the Hamcrest matcher.
   */
  static <T> void requireMatchBase(T item, Matcher<?> matcher, String itemDescription = null, String matcherDescription = null) {
    requireTrue(matcher != null)

    if (!matcher.matches(item)) {
      String myItemDescription = itemDescription?.trim() ?: item
      String myMatcherDescription = matcherDescription?.trim() ?: new StringDescription().appendDescriptionOf(matcher).toString()
      String assertionErrorMessage = "${ REQUIRE_MATCH_MESSAGE_DEFAULT } - [item: $myItemDescription, expected: $myMatcherDescription, actual: ${ item }]"

      throw new AssertionError(assertionErrorMessage as Object)
    }
  }
}
