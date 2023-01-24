package org.klokwrk.cargotracker.lib.test.support.web

import groovy.transform.CompileStatic

/**
 * The interface which must be implemented by classes responsible for asserting individual elements of {@code payload.pageContent} part in the response map.
 * <p/>
 * For more details, take a look at {@link ResponseContentPageablePayloadAssertion}.
 *
 * @see ResponseContentPageablePayloadAssertion
 */
@CompileStatic
interface PayloadPageContentAssertionable {
  <A> A isSuccessful()
}
