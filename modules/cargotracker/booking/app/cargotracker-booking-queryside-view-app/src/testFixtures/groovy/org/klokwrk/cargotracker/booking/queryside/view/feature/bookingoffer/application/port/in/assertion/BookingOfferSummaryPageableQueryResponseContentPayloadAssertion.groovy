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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentPageablePayloadAssertion

/**
 * Assertion class for a responses of BookingOfferSummaryFindAll and BookingOfferSummarySearchAll queries.
 * <p/>
 * BookingOfferSummaryFindAll and BookingOfferSummarySearchAll queries have the response structured in identical way, where that response is organized as a page of content. Each individual element
 * of that page is asserted by {@link BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion} instances.
 * <p/>
 * Therefore, this class extends {@link ResponseContentPageablePayloadAssertion}, and has to provide itself as a type (for a parent to be able to return "{@code this}" instances in a type-safe way),
 * and a {@link BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion} responsible for asserting individual elements of a page.
 *
 * @see ResponseContentPageablePayloadAssertion
 */
@SuppressWarnings("CodeNarc.BracesForClass")
@CompileStatic
class BookingOfferSummaryPageableQueryResponseContentPayloadAssertion
    extends ResponseContentPageablePayloadAssertion<BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion>
{
  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertResponseContentHasPageablePayloadThat(Map responseContentMap) {
    assertResponseContent(responseContentMap)
    return new BookingOfferSummaryPageableQueryResponseContentPayloadAssertion(responseContentMap)
  }

  BookingOfferSummaryPageableQueryResponseContentPayloadAssertion(Map responseContentMap) {
    super(responseContentMap)
  }

  @Override
  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion getPageContentAssertionInstance(Map pageContentElementPayloadMap) {
    return new BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion(pageContentElementPayloadMap)
  }
}
