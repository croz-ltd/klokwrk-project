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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.adapter.out.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryHandler
import org.klokwrk.cargotracker.booking.queryside.model.rdbms.jpa.BookingOfferDetailsJpaEntity
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.json.RawJsonWrapper
import org.springframework.stereotype.Service

@Service
@CompileStatic
class BookingOfferDetailsQueryHandlerService {
  private final BookingOfferDetailsViewJpaRepository bookingOfferDetailsViewJpaRepository
  private final ObjectMapper objectMapper

  BookingOfferDetailsQueryHandlerService(BookingOfferDetailsViewJpaRepository bookingOfferDetailsViewJpaRepository, ObjectMapper objectMapper) {
    this.bookingOfferDetailsViewJpaRepository = bookingOfferDetailsViewJpaRepository
    this.objectMapper = objectMapper
  }

  @QueryHandler
  BookingOfferDetailsFindByIdQueryResponse handleBookingOfferDetailsFindByIdQueryRequest(BookingOfferDetailsFindByIdQueryRequest bookingOfferDetailsFindByIdQueryRequest) {
    BookingOfferDetailsJpaEntity bookingOfferDetailsJpaEntity = bookingOfferDetailsViewJpaRepository
        .findByBookingOfferIdAndCustomerId(UUID.fromString(bookingOfferDetailsFindByIdQueryRequest.bookingOfferId), bookingOfferDetailsFindByIdQueryRequest.customerId)

    if (!bookingOfferDetailsJpaEntity) {
      throw new QueryException(ViolationInfo.NOT_FOUND)
    }

    BookingOfferDetailsFindByIdQueryResponse bookingOfferDetailsFindByIdQueryResponse = new BookingOfferDetailsFindByIdQueryResponse(fetchBookingOfferDetailsJpaEntityProperties(bookingOfferDetailsJpaEntity))
    return bookingOfferDetailsFindByIdQueryResponse
  }

  protected Map<String, Object> fetchBookingOfferDetailsJpaEntityProperties(BookingOfferDetailsJpaEntity bookingOfferDetailsJpaEntity) {
    return bookingOfferDetailsJpaEntity.properties.tap({
      it["detailsRaw"] = new RawJsonWrapper(rawJson: bookingOfferDetailsJpaEntity.details)
    })
  }
}
