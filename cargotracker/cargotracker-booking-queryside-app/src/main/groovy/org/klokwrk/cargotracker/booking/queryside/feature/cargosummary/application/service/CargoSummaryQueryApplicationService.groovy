/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.service

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryGateway
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.CargoSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.CargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryGatewayAdapter
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaData
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

@Service
@CompileStatic
class CargoSummaryQueryApplicationService implements CargoSummaryQueryPortIn {
  private final QueryGatewayAdapter queryGatewayAdapter
  private final ValidationService validationService

  CargoSummaryQueryApplicationService(ValidationService validationService, QueryGateway queryGateway) {
    this.validationService = validationService
    this.queryGatewayAdapter = new QueryGatewayAdapter(queryGateway)
  }

  @Override
  OperationResponse<FetchCargoSummaryQueryResponse> cargoSummaryQuery(OperationRequest<CargoSummaryQueryRequest> fetchCargoSummaryQueryOperationRequest) {
    requireMatch(fetchCargoSummaryQueryOperationRequest, notNullValue())
    validationService.validate(fetchCargoSummaryQueryOperationRequest.payload)

    FetchCargoSummaryQueryResponse fetchCargoSummaryQueryResponse = queryGatewayAdapter.query(fetchCargoSummaryQueryOperationRequest, FetchCargoSummaryQueryResponse)
    return fetchCargoSummaryQueryOperationResponseFromFetchCargoSummaryQueryResponse(fetchCargoSummaryQueryResponse)
  }

  protected OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQueryOperationResponseFromFetchCargoSummaryQueryResponse(FetchCargoSummaryQueryResponse fetchCargoSummaryQueryResponse) {
    ResponseMetaData responseMetaData = ResponseMetaData.createBasicInfoResponseMetaData()
    return new OperationResponse<FetchCargoSummaryQueryResponse>(payload: fetchCargoSummaryQueryResponse, metaData: responseMetaData.propertiesFiltered)
  }
}
