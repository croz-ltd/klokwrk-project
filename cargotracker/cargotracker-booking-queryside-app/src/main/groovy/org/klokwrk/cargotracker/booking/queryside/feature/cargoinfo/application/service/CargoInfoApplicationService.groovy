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
package org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.service

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in.CargoSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in.BookingOfferSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargoinfo.application.port.in.BookingOfferSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ResponseMetaData
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class CargoInfoApplicationService implements CargoSummaryQueryPortIn {
  private final QueryGatewayAdapter queryGatewayAdapter
  private final ValidationService validationService

  CargoInfoApplicationService(ValidationService validationService, QueryGateway queryGateway) {
    this.validationService = validationService
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  @Override
  OperationResponse<BookingOfferSummaryQueryResponse> cargoSummaryQuery(OperationRequest<BookingOfferSummaryQueryRequest> bookingOfferSummaryQueryOperationRequest) {
    requireMatch(bookingOfferSummaryQueryOperationRequest, notNullValue())
    validationService.validate(bookingOfferSummaryQueryOperationRequest.payload)

    BookingOfferSummaryQueryResponse bookingOfferSummaryQueryResponse = queryGatewayAdapter.query(bookingOfferSummaryQueryOperationRequest, BookingOfferSummaryQueryResponse)
    return cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(bookingOfferSummaryQueryResponse)
  }

  protected OperationResponse<BookingOfferSummaryQueryResponse> cargoSummaryQueryOperationResponseFromCargoSummaryQueryResponse(BookingOfferSummaryQueryResponse bookingOfferSummaryQueryResponse) {
    ResponseMetaData responseMetaData = ResponseMetaData.createBasicInfoResponseMetaData()
    return new OperationResponse<BookingOfferSummaryQueryResponse>(payload: bookingOfferSummaryQueryResponse, metaData: responseMetaData.propertiesFiltered)
  }
}
