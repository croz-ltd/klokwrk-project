/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryPortIn
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryRequest
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryResponse
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CompileStatic
@RestController
@RequestMapping("/cargo-summary")
class CargoSummaryQueryWebController {
  private final FetchCargoSummaryQueryPortIn fetchCargoSummaryQueryPortIn

  CargoSummaryQueryWebController(FetchCargoSummaryQueryPortIn fetchCargoSummaryQueryPortIn) {
    this.fetchCargoSummaryQueryPortIn = fetchCargoSummaryQueryPortIn
  }

  @PostMapping("/fetch-cargo-summary")
  OperationResponse<FetchCargoSummaryQueryResponse> fetchCargoSummaryQuery(@RequestBody FetchCargoSummaryQueryWebRequest webRequest, Locale locale) {
    OperationResponse<FetchCargoSummaryQueryResponse> cargoSummary = fetchCargoSummaryQueryPortIn.fetchCargoSummaryQuery(createOperationRequest(webRequest, FetchCargoSummaryQueryRequest, locale))
    return cargoSummary
  }

  /**
   * Creates {@link OperationRequest} from <code>webRequest</code> DTO.
   *
   * @param <P> Type of the {@link OperationRequest}'s payload.
   */
  private <P> OperationRequest<P> createOperationRequest(Object webRequest, Class<P> operationRequestPayloadType, Locale locale) {
    OperationRequest<P> operationRequest = new OperationRequest(
        payload: operationRequestPayloadType.newInstance(webRequest.properties),
        metaData: [(MetaDataConstant.INBOUND_CHANNEL_REQUEST_LOCALE_KEY): locale]
    )

    return operationRequest
  }
}
