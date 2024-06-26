/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.booking.app.commandside.test.util

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@CompileStatic
class BookingOfferCommandTestHelpers {
  static final String CREATE_BOOKING_OFFER_URL_PATH = "/booking-offer/create-booking-offer"

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static Map createBookingOffer_succeeded(String webRequestBody, String acceptLanguageHeaderValue, MockMvc mockMvc) {
    return makeRequestAndReturnResponseContentMap(webRequestBody, CREATE_BOOKING_OFFER_URL_PATH, acceptLanguageHeaderValue, OK.value(), mockMvc)
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  static Map createBookingOffer_failed(String webRequestBody, String acceptLanguageHeaderValue, MockMvc mockMvc) {
    return makeRequestAndReturnResponseContentMap(webRequestBody, CREATE_BOOKING_OFFER_URL_PATH, acceptLanguageHeaderValue, BAD_REQUEST.value(), mockMvc)
  }

  private static Map makeRequestAndReturnResponseContentMap(String webRequestBody, String urlPath, String acceptLanguageHeaderValue, Integer expectedResponseStatus, MockMvc mockMvc) {
    MvcResult mvcResult = makeRequest(webRequestBody, urlPath, acceptLanguageHeaderValue, mockMvc)

    assert mvcResult.response.status == expectedResponseStatus
    assert mvcResult.response.contentType == MediaType.APPLICATION_JSON_VALUE

    Map responseContentMap = new JsonSlurper().parseText(mvcResult.response.contentAsString) as Map
    return responseContentMap
  }

  private static MvcResult makeRequest(String webRequestBody, String urlPath, String acceptLanguageHeaderValue, MockMvc mockMvc) {
    MvcResult mvcResult = mockMvc.perform(
        post(urlPath)
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguageHeaderValue)
    ).andReturn()

    return mvcResult
  }
}
