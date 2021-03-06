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
package org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFactory

/**
 * Contains test metadata fixtures for <code>web-booking</code> inbound channel.
 */
@CompileStatic
class WebMetaDataFixtures {
  /**
   * Creates valid metadata for <code>web-booking</code> inbound channel.
   */
  static Map<String, ?> metaDataMapForWebBookingChannel() {
    return WebMetaDataFactory.createMetaDataMapForWebBookingChannel("127.0.0.1")
  }
}
