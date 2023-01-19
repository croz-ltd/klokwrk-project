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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.response

import groovy.transform.CompileStatic

@CompileStatic
class BookingOfferSummarySearchAllQueryResponseWebContentPayloadAssertion {
  static BookingOfferSummarySearchAllQueryResponseWebContentPayloadAssertion assertWebResponseContentHasPayloadThat(Map webResponseContentMap) {
    webResponseContentMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new BookingOfferSummarySearchAllQueryResponseWebContentPayloadAssertion(webResponseContentMap.payload as Map)
  }

  private final Map payloadMap

  BookingOfferSummarySearchAllQueryResponseWebContentPayloadAssertion(Map payloadMap) {
    this.payloadMap = payloadMap
  }

  BookingOfferSummarySearchAllQueryResponseWebContentPayloadAssertion isEmpty() {
    assert payloadMap.size() == 0
    return this
  }
}
