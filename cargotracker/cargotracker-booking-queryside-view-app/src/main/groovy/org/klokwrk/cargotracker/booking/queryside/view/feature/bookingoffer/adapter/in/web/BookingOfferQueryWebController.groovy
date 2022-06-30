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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryResponse
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
class BookingOfferQueryWebController {
  private final BookingOfferSummaryFindByIdQueryPortIn bookingOfferSummaryFindByIdQueryPortIn
  private final BookingOfferSummaryFindAllQueryPortIn bookingOfferSummaryFindAllQueryPortIn

  BookingOfferQueryWebController(BookingOfferSummaryFindByIdQueryPortIn bookingOfferSummaryFindByIdQueryPortIn, BookingOfferSummaryFindAllQueryPortIn bookingOfferSummaryFindAllQueryPortIn) {
    this.bookingOfferSummaryFindByIdQueryPortIn = bookingOfferSummaryFindByIdQueryPortIn
    this.bookingOfferSummaryFindAllQueryPortIn = bookingOfferSummaryFindAllQueryPortIn
  }

  @PostMapping("/booking-offer-summary-find-by-id")
  OperationResponse<BookingOfferSummaryFindByIdQueryResponse> bookingOfferSummaryFindByIdQuery(@RequestBody BookingOfferSummaryFindByIdQueryWebRequest webRequest, Locale locale) {
    OperationResponse<BookingOfferSummaryFindByIdQueryResponse> bookingOfferSummaryFindByIdOperationResponse =
        bookingOfferSummaryFindByIdQueryPortIn.bookingOfferSummaryFindByIdQuery(makeOperationRequest(webRequest, BookingOfferSummaryFindByIdQueryRequest, locale))

    return bookingOfferSummaryFindByIdOperationResponse
  }

  /**
   * Creates {@link OperationRequest} from {@code webRequest} DTO.
   *
   * @param <P> Type of the {@link OperationRequest}'s payload.
   */
  protected <P> OperationRequest<P> makeOperationRequest(Object webRequest, Class<P> operationRequestPayloadType, Locale locale) {
    //noinspection GrDeprecatedAPIUsage
    OperationRequest<P> operationRequest = new OperationRequest(
        payload: operationRequestPayloadType.newInstance(webRequest.propertiesFiltered),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    return operationRequest
  }

  @PostMapping("/booking-offer-summary-find-all")
  OperationResponse<BookingOfferSummaryFindAllQueryResponse> bookingOfferSummaryFindAllQuery(@RequestBody BookingOfferSummaryFindAllQueryRequest webRequest, Locale locale) {
    OperationResponse<BookingOfferSummaryFindAllQueryResponse> bookingOfferSummaryFindAllOperationResponse =
        bookingOfferSummaryFindAllQueryPortIn.bookingOfferSummaryFindAllQuery(makeOperationRequest(webRequest, BookingOfferSummaryFindAllQueryRequest, locale))

    return bookingOfferSummaryFindAllOperationResponse
  }
}
