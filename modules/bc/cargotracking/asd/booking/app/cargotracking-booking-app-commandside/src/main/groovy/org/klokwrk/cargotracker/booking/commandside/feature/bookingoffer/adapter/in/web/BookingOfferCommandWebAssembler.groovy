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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataFactory
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.web.util.ClientIpAddressExtractor

import jakarta.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-applicationLayer assemblers should live and how to operate.
@CompileStatic
class BookingOfferCommandWebAssembler {

  static OperationRequest<CreateBookingOfferCommandRequest> toCreateBookingOfferCommandOperationRequest(
      CreateBookingOfferCommandWebRequest createBookingOfferCommandWebRequest, HttpServletRequest httpServletRequest)
  {
    Map metadataMap = WebMetaDataFactory.makeMetaDataMapForWebBookingChannel(ClientIpAddressExtractor.extractClientIpAddress(httpServletRequest))

    OperationRequest<CreateBookingOfferCommandRequest> createBookingOfferCommandOperationRequest =
        new OperationRequest(payload: new CreateBookingOfferCommandRequest(createBookingOfferCommandWebRequest.properties), metaData: metadataMap)

    return createBookingOfferCommandOperationRequest
  }
}
