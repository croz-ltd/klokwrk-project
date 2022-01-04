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
package org.klokwrk.cargotracker.lib.web.util

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest

/**
 * Contains utility method trying to extract client's IP Address if possible.
 */
@CompileStatic
class ClientIpAddressExtractor {
  /**
   * The list of known HTTP headers used for carrying proxy server and original visitor IP addresses.
   */
  static final List<String> IP_HEADER_CANDIDATE_LIST = [
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR"
  ]

  /**
   * Tries to extract client's IP address by inspecting known HTTP headers.
   * <p/>
   * Uses {@code HttpServletRequest.getRemoteAddr()} as a fallback.
   */
  static String extractClientIpAddress(HttpServletRequest httpServletRequest) {
    String clientIp = null

    IP_HEADER_CANDIDATE_LIST.find { String ipHeaderCandidate ->
      String headerValue = httpServletRequest.getHeader(ipHeaderCandidate)
      String clientIpCandidate = headerValue?.tokenize(",")?.last()?.trim()
      if (clientIpCandidate && !clientIpCandidate.equalsIgnoreCase("unknown")) {
        clientIp = clientIpCandidate
        return true
      }

      return false
    }

    clientIp = clientIp ?: httpServletRequest.remoteAddr
    return clientIp
  }
}
