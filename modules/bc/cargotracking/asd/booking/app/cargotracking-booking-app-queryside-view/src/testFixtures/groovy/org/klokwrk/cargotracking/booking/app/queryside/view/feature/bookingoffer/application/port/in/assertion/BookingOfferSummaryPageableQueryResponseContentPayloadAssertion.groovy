/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.klokwrk.cargotracking.test.support.assertion.PageablePayloadAssertion

/**
 * Assertion class for a responses of BookingOfferSummaryFindAll and BookingOfferSummarySearchAll queries.
 * <p/>
 * BookingOfferSummaryFindAll and BookingOfferSummarySearchAll queries have the response structured in identical way, where that response is organized as a page of content. Each individual item
 * of that page is asserted by {@link BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion} instances.
 * <p/>
 * Therefore, this class extends {@link PageablePayloadAssertion}, and has to provide itself as a type (for a parent to be able to return "{@code this}" instances in a type-safe way),
 * and a {@link BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion} responsible for asserting individual items of a page.
 *
 * @see PageablePayloadAssertion
 */
@SuppressWarnings("CodeNarc.BracesForClass")
@CompileStatic
class BookingOfferSummaryPageableQueryResponseContentPayloadAssertion
    extends PageablePayloadAssertion<BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion>
{
  /**
   * Entry point static assertion method for fluent-style top-level API.
   */
  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertResponseHasPageablePayloadThat(Map responseMap) {
    assertResponse(responseMap)
    return new BookingOfferSummaryPageableQueryResponseContentPayloadAssertion(responseMap)
  }

  /**
   * Overloaded entry point static assertion method for closure-style top-level API.
   */
  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertResponseHasPageablePayloadThat(
      Map responseMap,
      @DelegatesTo(value = BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    BookingOfferSummaryPageableQueryResponseContentPayloadAssertion pageablePayloadAssertion = assertResponseHasPageablePayloadThat(responseMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = pageablePayloadAssertion
    aClosure.call(pageablePayloadAssertion)

    return pageablePayloadAssertion
  }

  BookingOfferSummaryPageableQueryResponseContentPayloadAssertion(Map responseMap) {
    super(responseMap)
  }

  @Override
  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion getPageItemAssertionInstance(Map pageItemMap) {
    return new BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion(pageItemMap)
  }
}
