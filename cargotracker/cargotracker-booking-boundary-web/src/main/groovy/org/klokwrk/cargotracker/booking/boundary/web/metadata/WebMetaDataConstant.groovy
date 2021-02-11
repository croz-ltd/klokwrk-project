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
package org.klokwrk.cargotracker.booking.boundary.web.metadata

import groovy.transform.CompileStatic

@CompileStatic
class WebMetaDataConstant {
  /**
   * Represents a value for <code>MetaDataConstant.INBOUND_CHANNEL_NAME_KEY</code> when corresponding inbound request is directed via cargotracker booking web channel.
   */
  static final String WEB_BOOKING_CHANNEL_NAME = "booking"

  /**
   * Represents a value for <code>MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY</code> when corresponding inbound request is directed via cargotracker booking web channel.
   */
  static final String WEB_BOOKING_CHANNEL_TYPE = "web"
}
