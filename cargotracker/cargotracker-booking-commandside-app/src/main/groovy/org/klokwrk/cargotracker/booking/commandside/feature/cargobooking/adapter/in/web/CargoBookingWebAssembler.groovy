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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFactory
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.web.util.ClientIpAddressExtractor

import javax.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-applicationLayer assemblers should live and how to operate.
@CompileStatic
class CargoBookingWebAssembler {

  static OperationRequest<BookCargoCommandRequest> toBookCargoCommandOperationRequest(BookCargoCommandWebRequest bookCargoCommandWebRequest, HttpServletRequest httpServletRequest) {
    Map metadataMap = WebMetaDataFactory.createMetaDataMapForWebBookingChannel(ClientIpAddressExtractor.extractClientIpAddress(httpServletRequest))

    OperationRequest<BookCargoCommandRequest> bookCargoCommandOperationRequest =
        new OperationRequest(payload: new BookCargoCommandRequest(bookCargoCommandWebRequest.properties), metaData: metadataMap)

    return bookCargoCommandOperationRequest
  }
}
