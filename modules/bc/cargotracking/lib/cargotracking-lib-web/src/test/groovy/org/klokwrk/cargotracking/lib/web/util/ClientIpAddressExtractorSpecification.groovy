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
package org.klokwrk.cargotracking.lib.web.util

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

class ClientIpAddressExtractorSpecification extends Specification {
  void "should work without headers"() {
    given:
    MockHttpServletRequest request = MockMvcRequestBuilders.get("http://some.com").buildRequest(null)

    when:
    String clientIpAddress = ClientIpAddressExtractor.extractClientIpAddress(request)

    then:
    clientIpAddress == MockHttpServletRequest.DEFAULT_REMOTE_ADDR
  }

  void "should work with header containing 'unknown'"() {
    given:
    MockHttpServletRequest request = MockMvcRequestBuilders
        .get("http://some.com")
        .header("X-Forwarded-For", "unknown")
        .buildRequest(null)

    when:
    String clientIpAddress = ClientIpAddressExtractor.extractClientIpAddress(request)

    then:
    clientIpAddress == MockHttpServletRequest.DEFAULT_REMOTE_ADDR
  }

  void "should work with header containing single ip  address"() {
    given:
    MockHttpServletRequest request = MockMvcRequestBuilders
        .get("http://some.com")
        .header(headerNameParam, "203.0.113.1")
        .buildRequest(null)

    when:
    String clientIpAddress = ClientIpAddressExtractor.extractClientIpAddress(request)

    then:
    clientIpAddress == "203.0.113.1"

    where:
    headerNameParam    | _
    "X-Forwarded-For"  | _
    "HTTP_X_FORWARDED" | _
  }

  void "should work with header containing multiple ip  address"() {
    given:
    MockHttpServletRequest request = MockMvcRequestBuilders
        .get("http://some.com")
        .header(headerNameParam, "198.51.100.101, 198.51.100.102, 203.0.113.1")
        .buildRequest(null)

    when:
    String clientIpAddress = ClientIpAddressExtractor.extractClientIpAddress(request)

    then:
    clientIpAddress == "203.0.113.1"

    where:
    headerNameParam    | _
    "X-Forwarded-For"  | _
    "HTTP_X_FORWARDED" | _
  }
}
