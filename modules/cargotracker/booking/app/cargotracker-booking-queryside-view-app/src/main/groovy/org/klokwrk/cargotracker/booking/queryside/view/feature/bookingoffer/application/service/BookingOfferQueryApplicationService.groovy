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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.service

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.out.customer.port.CustomerByUserIdentifierPortOut
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ResponseMetaData
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class BookingOfferQueryApplicationService implements BookingOfferSummaryFindByIdQueryPortIn, BookingOfferSummaryFindAllQueryPortIn, BookingOfferSummarySearchAllQueryPortIn {
  private final QueryGatewayAdapter queryGatewayAdapter
  private final ValidationService validationService
  private final CustomerByUserIdentifierPortOut customerByUserIdentifierPortOut

  BookingOfferQueryApplicationService(ValidationService validationService, QueryGateway queryGateway, CustomerByUserIdentifierPortOut customerByUserIdentifierPortOut) {
    this.validationService = validationService
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
    this.customerByUserIdentifierPortOut = customerByUserIdentifierPortOut
  }

  @Override
  OperationResponse<BookingOfferSummaryFindByIdQueryResponse> bookingOfferSummaryFindByIdQuery(
      OperationRequest<BookingOfferSummaryFindByIdQueryRequest> bookingOfferSummaryFindByIdQueryOperationRequest)
  {
    requireMatch(bookingOfferSummaryFindByIdQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryFindByIdQueryOperationRequest.payload)

    Customer customer = customerByUserIdentifierPortOut.findCustomerByUserIdentifier(bookingOfferSummaryFindByIdQueryOperationRequest.payload.userIdentifier)
    bookingOfferSummaryFindByIdQueryOperationRequest.payload.customerIdentifier = customer.customerId.identifier

    BookingOfferSummaryFindByIdQueryResponse bookingOfferSummaryFindByIdQueryResponse =
        queryGatewayAdapter.query(bookingOfferSummaryFindByIdQueryOperationRequest, BookingOfferSummaryFindByIdQueryResponse)

    return operationResponseFromQueryResponse(bookingOfferSummaryFindByIdQueryResponse)
  }

  protected <T> OperationResponse<T> operationResponseFromQueryResponse(T queryResponse) {
    ResponseMetaData responseMetaData = ResponseMetaData.makeBasicInfoResponseMetaData()

    // TODO dmurat: Remove CodeNarc disabling if and when https://issues.apache.org/jira/browse/GROOVY-10815 will be fixed.
    //              Also remove for other usages of getPropertiesFiltered().
    return new OperationResponse<T>(payload: queryResponse, metaData: responseMetaData.getPropertiesFiltered()) // codenarc-disable-line UnnecessaryGetter
  }

  @Override
  OperationResponse<BookingOfferSummaryFindAllQueryResponse> bookingOfferSummaryFindAllQuery(
      OperationRequest<BookingOfferSummaryFindAllQueryRequest> bookingOfferSummaryFindAllQueryOperationRequest)
  {
    requireMatch(bookingOfferSummaryFindAllQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryFindAllQueryOperationRequest.payload)

    Customer customer = customerByUserIdentifierPortOut.findCustomerByUserIdentifier(bookingOfferSummaryFindAllQueryOperationRequest.payload.userIdentifier)
    bookingOfferSummaryFindAllQueryOperationRequest.payload.customerIdentifier = customer.customerId.identifier

    BookingOfferSummaryFindAllQueryResponse queryResponse = queryGatewayAdapter.query(bookingOfferSummaryFindAllQueryOperationRequest, BookingOfferSummaryFindAllQueryResponse)
    return operationResponseFromQueryResponse(queryResponse)
  }

  @Override
  OperationResponse<BookingOfferSummarySearchAllQueryResponse> bookingOfferSummarySearchAllQuery(
      OperationRequest<BookingOfferSummarySearchAllQueryRequest> bookingOfferSummarySearchAllQueryOperationRequest)
  {
    requireMatch(bookingOfferSummarySearchAllQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummarySearchAllQueryOperationRequest.payload)

    Customer customer = customerByUserIdentifierPortOut.findCustomerByUserIdentifier(bookingOfferSummarySearchAllQueryOperationRequest.payload.userIdentifier)
    bookingOfferSummarySearchAllQueryOperationRequest.payload.customerIdentifier = customer.customerId.identifier

    BookingOfferSummarySearchAllQueryResponse queryResponse = queryGatewayAdapter.query(bookingOfferSummarySearchAllQueryOperationRequest, BookingOfferSummarySearchAllQueryResponse)
    return operationResponseFromQueryResponse(queryResponse)
  }
}
