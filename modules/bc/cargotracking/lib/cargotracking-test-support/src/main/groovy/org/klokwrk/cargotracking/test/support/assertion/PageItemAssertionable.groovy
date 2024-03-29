package org.klokwrk.cargotracking.test.support.assertion

import groovy.transform.CompileStatic

/**
 * The interface which must be implemented by classes responsible for asserting individual items of {@code payload.pageContent} part in the response map.
 * <p/>
 * For more details, take a look at {@link PageablePayloadAssertion}.
 *
 * @see PageablePayloadAssertion
 */
@CompileStatic
interface PageItemAssertionable {
  <A> A isSuccessful()
}
