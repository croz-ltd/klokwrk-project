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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.out.customer.port.CustomerByUserIdPortOut
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferDetailsFindByIdQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindAllQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummaryFindByIdQueryResponse
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryRequest
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.BookingOfferSummarySearchAllQueryResponse
import org.klokwrk.cargotracking.lib.axon.cqrs.query.QueryGatewayAdapter
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ResponseMetaData
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.hi.validation.springboot.ValidationService
import org.springframework.stereotype.Service
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.math.RoundingMode

import static org.hamcrest.Matchers.notNullValue

@SuppressWarnings("CodeNarc.BracesForClass")
@Service
@CompileStatic
class BookingOfferQueryApplicationService
    implements BookingOfferSummaryFindByIdQueryPortIn, BookingOfferSummaryFindAllQueryPortIn, BookingOfferSummarySearchAllQueryPortIn, BookingOfferDetailsFindByIdQueryPortIn
{
  private final QueryGatewayAdapter queryGatewayAdapter
  private final ValidationService validationService
  private final CustomerByUserIdPortOut customerByUserIdPortOut
  private final ObjectMapper objectMapper

  BookingOfferQueryApplicationService(ValidationService validationService, QueryGateway queryGateway, CustomerByUserIdPortOut customerByUserIdPortOut, ObjectMapper objectMapper) {
    this.validationService = validationService
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
    this.customerByUserIdPortOut = customerByUserIdPortOut
    this.objectMapper = objectMapper
  }

  @Override
  OperationResponse<BookingOfferSummaryFindByIdQueryResponse> bookingOfferSummaryFindByIdQuery(
      OperationRequest<BookingOfferSummaryFindByIdQueryRequest> bookingOfferSummaryFindByIdQueryOperationRequest)
  {
    requireMatch(bookingOfferSummaryFindByIdQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryFindByIdQueryOperationRequest.payload)

    Customer customer = customerByUserIdPortOut.findCustomerByUserId(bookingOfferSummaryFindByIdQueryOperationRequest.payload.userId)
    bookingOfferSummaryFindByIdQueryOperationRequest.payload.customerId = customer.customerId.identifier

    BookingOfferSummaryFindByIdQueryResponse bookingOfferSummaryFindByIdQueryResponse =
        queryGatewayAdapter.query(bookingOfferSummaryFindByIdQueryOperationRequest, BookingOfferSummaryFindByIdQueryResponse)

    return operationResponseFromQueryResponse(bookingOfferSummaryFindByIdQueryResponse)
  }

  @Override
  OperationResponse<BookingOfferSummaryFindAllQueryResponse> bookingOfferSummaryFindAllQuery(
      OperationRequest<BookingOfferSummaryFindAllQueryRequest> bookingOfferSummaryFindAllQueryOperationRequest)
  {
    requireMatch(bookingOfferSummaryFindAllQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryFindAllQueryOperationRequest.payload)

    Customer customer = customerByUserIdPortOut.findCustomerByUserId(bookingOfferSummaryFindAllQueryOperationRequest.payload.userId)
    bookingOfferSummaryFindAllQueryOperationRequest.payload.customerId = customer.customerId.identifier

    BookingOfferSummaryFindAllQueryResponse queryResponse = queryGatewayAdapter.query(bookingOfferSummaryFindAllQueryOperationRequest, BookingOfferSummaryFindAllQueryResponse)
    return operationResponseFromQueryResponse(queryResponse)
  }

  @Override
  OperationResponse<BookingOfferSummarySearchAllQueryResponse> bookingOfferSummarySearchAllQuery(
      OperationRequest<BookingOfferSummarySearchAllQueryRequest> bookingOfferSummarySearchAllQueryOperationRequest)
  {
    requireMatch(bookingOfferSummarySearchAllQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummarySearchAllQueryOperationRequest.payload)

    Customer customer = customerByUserIdPortOut.findCustomerByUserId(bookingOfferSummarySearchAllQueryOperationRequest.payload.userId)
    bookingOfferSummarySearchAllQueryOperationRequest.payload.customerId = customer.customerId.identifier

    BookingOfferSummarySearchAllQueryResponse queryResponse = queryGatewayAdapter.query(
        prepareBookingOfferSummarySearchAllQueryRequest(bookingOfferSummarySearchAllQueryOperationRequest), BookingOfferSummarySearchAllQueryResponse
    )
    return operationResponseFromQueryResponse(queryResponse)
  }

  protected OperationRequest<BookingOfferSummarySearchAllQueryRequest> prepareBookingOfferSummarySearchAllQueryRequest(
      OperationRequest<BookingOfferSummarySearchAllQueryRequest> bookingOfferSummarySearchAllQueryRequest)
  {
    Quantity<Mass> totalCommodityWeightFromIncluding = bookingOfferSummarySearchAllQueryRequest.payload.totalCommodityWeightFromIncluding
    if (totalCommodityWeightFromIncluding) {
      Long totalCommodityWeightKgFromIncluding = totalCommodityWeightFromIncluding.to(Units.KILOGRAM).value.toBigDecimal().setScale(0, RoundingMode.HALF_DOWN).toLong()
      bookingOfferSummarySearchAllQueryRequest.payload.totalCommodityWeightKgFromIncluding = totalCommodityWeightKgFromIncluding
    }

    Quantity<Mass> totalCommodityWeightToIncluding = bookingOfferSummarySearchAllQueryRequest.payload.totalCommodityWeightToIncluding
    if (totalCommodityWeightToIncluding) {
      Long totalCommodityWeightKgToIncluding = totalCommodityWeightToIncluding.to(Units.KILOGRAM).value.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toLong()
      bookingOfferSummarySearchAllQueryRequest.payload.totalCommodityWeightKgToIncluding = totalCommodityWeightKgToIncluding
    }

    return bookingOfferSummarySearchAllQueryRequest
  }

  @Override
  OperationResponse<BookingOfferDetailsFindByIdQueryResponse> bookingOfferDetailsFindByIdQuery(
      OperationRequest<BookingOfferDetailsFindByIdQueryRequest> bookingOfferDetailsFindByIdQueryOperationRequest)
  {
    requireMatch(bookingOfferDetailsFindByIdQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferDetailsFindByIdQueryOperationRequest.payload)

    Customer customer = customerByUserIdPortOut.findCustomerByUserId(bookingOfferDetailsFindByIdQueryOperationRequest.payload.userId)
    bookingOfferDetailsFindByIdQueryOperationRequest.payload.customerId = customer.customerId.identifier

    BookingOfferDetailsFindByIdQueryResponse queryResponse = queryGatewayAdapter.query(bookingOfferDetailsFindByIdQueryOperationRequest, BookingOfferDetailsFindByIdQueryResponse)

    Map<String, ?> operationResponsePropertiesMap = objectMapper.readValue(queryResponse.detailsRaw.rawJson, Map)
    operationResponsePropertiesMap = queryResponse.getPropertiesFiltered(["class", "detailsRaw"]) + operationResponsePropertiesMap
    BookingOfferDetailsFindByIdQueryResponse operationResponse = new BookingOfferDetailsFindByIdQueryResponse(operationResponsePropertiesMap)

    return operationResponseFromQueryResponse(operationResponse)
  }

  protected <T> OperationResponse<T> operationResponseFromQueryResponse(T queryResponse) {
    ResponseMetaData responseMetaData = ResponseMetaData.makeBasicInfoResponseMetaData()
    return new OperationResponse<T>(payload: queryResponse, metaData: responseMetaData.propertiesFiltered)
  }
}
