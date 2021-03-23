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
package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.adapter.in.web

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.Sql
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracker.booking.queryside.test.base.AbstractQuerySideIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import javax.sql.DataSource
import java.nio.charset.Charset

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoSummaryQueryWebControllerIntegrationSpecification extends AbstractQuerySideIntegrationSpecification {
  @TestConfiguration
  static class TestSpringBootConfiguration {
    @Bean
    Sql groovySql(DataSource dataSource) {
      return new Sql(dataSource)
    }
  }

  @Autowired
  EventBus eventBus

  @Autowired
  Sql groovySql

  @Autowired
  WebApplicationContext webApplicationContext

  @Autowired
  ObjectMapper objectMapper

  MockMvc mockMvc

  void setup() {
    mockMvc ?= webAppContextSetup(webApplicationContext).build()
  }

  void "should work for correct request - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = publishAndWaitForProjectedCargoBookedEvent(eventBus, groovySql)
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-summary/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.OK.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      it.size() == 3
      locale == localeString
      severity == Severity.INFO.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      it.size() == 2
      message == HttpStatus.OK.reasonPhrase
      status == HttpStatus.OK.value().toString()
    }

    verifyAll(responseContentMap.payload as Map) {
      aggregateIdentifier == myAggregateIdentifier
      aggregateSequenceNumber == 0
      originLocation == "HRRJK"
      destinationLocation == "HRZAG"
    }

    where:
    acceptLanguage | localeString
    "hr-HR"        | "hr_HR"
    "en"           | "en"
  }

  @SuppressWarnings("AbcMetric")
  void "should return expected response when request is not valid - validation failure - [acceptLanguage: #acceptLanguage]"() {
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: null])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-summary/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.BAD_REQUEST.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      it.size() == 3
      locale == localeString
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      it.size() == 2
      message == HttpStatus.BAD_REQUEST.reasonPhrase
      status == HttpStatus.BAD_REQUEST.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      it.size() == 4
      code == HttpStatus.BAD_REQUEST.value().toString()
      codeMessage == myViolationCodeMessage
      type == ViolationType.VALIDATION.name().toLowerCase()
      validationReport != null
    }

    verifyAll(responseContentMap.metaData.violation.validationReport as Map) {
      it.size() == 2
      root.type == "fetchCargoSummaryQueryRequest"
      constraintViolations.size() == 1
      constraintViolations.find({ it.path == "aggregateIdentifier" }).type == "notBlank"
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguage | localeString | myViolationCodeMessage
    "hr-HR"        | "hr_HR"      | "Zahtjev nije ispravan."
    "en"           | "en"         | "Request is not valid."
  }

  void "should return expected response when CargoSummary cannot be found - domain failure - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier])

    when:
    MvcResult mvcResult = mockMvc.perform(
        post("/cargo-summary/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.NOT_FOUND.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      it.size() == 3
      locale == localeString
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      it.size() == 2
      message == HttpStatus.NOT_FOUND.reasonPhrase
      status == HttpStatus.NOT_FOUND.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      it.size() == 3
      code == HttpStatus.NOT_FOUND.value().toString()
      codeMessage == myViolationCodeMessage
      type == ViolationType.DOMAIN.name().toLowerCase()
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguage | localeString | myViolationCodeMessage
    "hr-HR"        | "hr_HR"      | "Sumarni izvještaj za željeni teret nije pronađen."
    "en"           | "en"         | "Summary report for specified cargo is not found."
  }

  void "should return expected response for a request with invalid HTTP method - [acceptLanguage: #acceptLanguage]"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    String webRequestBody = objectMapper.writeValueAsString([aggregateIdentifier: myAggregateIdentifier])

    when:
    MvcResult mvcResult = mockMvc.perform(
        put("/cargo-summary/fetch-cargo-summary")
            .content(webRequestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
            .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
    ).andReturn()

    Map responseContentMap = objectMapper.readValue(mvcResult.response.getContentAsString(Charset.forName("UTF-8")), Map)

    then:
    mvcResult.response.status == HttpStatus.METHOD_NOT_ALLOWED.value()

    verifyAll(responseContentMap.metaData.general as Map) {
      it.size() == 3
      locale == localeString
      severity == Severity.WARNING.name().toLowerCase()
      timestamp
    }

    verifyAll(responseContentMap.metaData.http as Map) {
      it.size() == 2
      message == HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase
      status == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
    }

    verifyAll(responseContentMap.metaData.violation as Map) {
      it.size() == 3
      code == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
      codeMessage == myViolationCodeMessage
      type == ViolationType.INFRASTRUCTURE_WEB.name().toLowerCase()
    }

    verifyAll(responseContentMap.payload as Map) {
      size() == 0
    }

    where:
    acceptLanguage | localeString | myViolationCodeMessage
    "hr-HR"        | "hr_HR"      | "Zahtjev nije ispravan."
    "en"           | "en"         | "Request is not valid."
  }
}
