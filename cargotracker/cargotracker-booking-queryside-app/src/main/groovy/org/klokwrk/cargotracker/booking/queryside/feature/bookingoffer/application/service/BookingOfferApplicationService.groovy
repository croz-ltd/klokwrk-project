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
package org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.service

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.out.customer.port.CustomerByUserIdentifierPortOut
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.bookingoffer.application.port.in.BookingOfferSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ResponseMetaData
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class BookingOfferApplicationService implements BookingOfferSummaryQueryPortIn {
  private final QueryGatewayAdapter queryGatewayAdapter
  private final ValidationService validationService
  private final CustomerByUserIdentifierPortOut customerByUserIdentifierPortOut

  BookingOfferApplicationService(ValidationService validationService, QueryGateway queryGateway, CustomerByUserIdentifierPortOut customerByUserIdentifierPortOut) {
    this.validationService = validationService
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
    this.customerByUserIdentifierPortOut = customerByUserIdentifierPortOut
  }

  @Override
  OperationResponse<BookingOfferSummaryQueryResponse> bookingOfferSummaryQuery(OperationRequest<BookingOfferSummaryQueryRequest> bookingOfferSummaryQueryOperationRequest) {
    requireMatch(bookingOfferSummaryQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryQueryOperationRequest.payload)

    Customer customer = customerByUserIdentifierPortOut.findCustomerByUserIdentifier(bookingOfferSummaryQueryOperationRequest.payload.userIdentifier)
    bookingOfferSummaryQueryOperationRequest.payload.customerIdentifier = customer.customerId.identifier

    BookingOfferSummaryQueryResponse bookingOfferSummaryQueryResponse = queryGatewayAdapter.query(bookingOfferSummaryQueryOperationRequest, BookingOfferSummaryQueryResponse)
    return operationResponseFromQueryResponse(bookingOfferSummaryQueryResponse)
  }

  protected OperationResponse<BookingOfferSummaryQueryResponse> operationResponseFromQueryResponse(BookingOfferSummaryQueryResponse bookingOfferSummaryQueryResponse) {
    ResponseMetaData responseMetaData = ResponseMetaData.makeBasicInfoResponseMetaData()
    return new OperationResponse<BookingOfferSummaryQueryResponse>(payload: bookingOfferSummaryQueryResponse, metaData: responseMetaData.propertiesFiltered)
  }
}
