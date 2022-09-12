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
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.lib.boundary.api.domain.metadata.constant.MetaDataConstant

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class WebMetaDataFixtureBuilder {
  static WebMetaDataFixtureBuilder webMetaData_booking_default() {
    WebMetaDataFixtureBuilder builder = new WebMetaDataFixtureBuilder().inboundChannelRequestIdentifier("127.0.0.1")
    return builder
  }

  String channelName = WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME
  String channelType = WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE
  String inboundChannelRequestIdentifier

  Map<String, ?> build() {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): channelName,
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): channelType,
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): inboundChannelRequestIdentifier
    ]

    return metadataMap
  }
}
