/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryResponse
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CompileStatic
@RestController
@RequestMapping("/booking-offer")
class BookingOfferWebController {
  private final BookingOfferSummaryQueryPortIn bookingOfferSummaryQueryPortIn

  BookingOfferWebController(BookingOfferSummaryQueryPortIn bookingOfferSummaryQueryPortIn) {
    this.bookingOfferSummaryQueryPortIn = bookingOfferSummaryQueryPortIn
  }

  @PostMapping("/booking-offer-summary")
  OperationResponse<BookingOfferSummaryQueryResponse> bookingOfferSummaryQuery(@RequestBody BookingOfferSummaryQueryWebRequest webRequest, Locale locale) {
    OperationResponse<BookingOfferSummaryQueryResponse> bookingOfferSummary =
        bookingOfferSummaryQueryPortIn.bookingOfferSummaryQuery(makeOperationRequest(webRequest, BookingOfferSummaryQueryRequest, locale))

    return bookingOfferSummary
  }

  /**
   * Creates {@link OperationRequest} from {@code webRequest} DTO.
   *
   * @param <P> Type of the {@link OperationRequest}'s payload.
   */
  private <P> OperationRequest<P> makeOperationRequest(Object webRequest, Class<P> operationRequestPayloadType, Locale locale) {
    OperationRequest<P> operationRequest = new OperationRequest(
        payload: operationRequestPayloadType.newInstance(webRequest.properties),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    return operationRequest
  }
}