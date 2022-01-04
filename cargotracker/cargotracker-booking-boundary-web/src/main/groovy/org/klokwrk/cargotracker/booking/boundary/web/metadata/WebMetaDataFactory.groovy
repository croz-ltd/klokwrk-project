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
package org.klokwrk.cargotracker.booking.boundary.web.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant

@CompileStatic
class WebMetaDataFactory {

  /**
   * Creates a simple map representing meta data for <code>web-booking</code> inbound channel.
   * <p/>
   * Parameter <code>inboundChannelRequestIdentifier</code> should specify unique identifier of web request, i.e. IP address of remote user.
   */
  static Map<String, ?> createMetaDataMapForWebBookingChannel(String inboundChannelRequestIdentifier) {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME,
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE,
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): inboundChannelRequestIdentifier
    ]

    return metadataMap
  }
}
