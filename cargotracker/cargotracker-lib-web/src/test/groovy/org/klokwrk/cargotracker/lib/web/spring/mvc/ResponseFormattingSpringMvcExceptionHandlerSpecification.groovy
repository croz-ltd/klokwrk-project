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
package org.klokwrk.cargotracker.lib.web.spring.mvc

import groovy.json.JsonSlurper
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import spock.lang.Specification
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@WebAppConfiguration
@ContextConfiguration(classes = [WebConfig])
class ResponseFormattingSpringMvcExceptionHandlerSpecification extends Specification {

  @RestController
  @RequestMapping("/test")
  static class SpringMvcExceptionHandlerTestController {
    @PostMapping("/springMvcExceptionHandlerTestControllerMethod")
    OperationResponse<Map> testControllerMethod() {
      return new OperationResponse<Map>(payload: [data: "Some testing data"])
    }

    @SuppressWarnings(["unused", "MVCPathVariableInspection", "CodeNarc.UnusedMethodParameter"])
    @PostMapping("/springMvcExceptionHandlerInvalidTestControllerMethod")
    OperationResponse<Map> invalidTestControllerMethod(@PathVariable String nonExistingPathVariable) {
      return new OperationResponse<Map>(payload: [data: "Some testing data"])
    }
  }

  @ControllerAdvice
  static class ResponseFormattingSpringMvcExceptionHandlerControllerAdvice extends ResponseFormattingSpringMvcExceptionHandler {
  }

  @EnableWebMvc
  @Configuration
  static class WebConfig implements WebMvcConfigurer {
    @Bean
    SpringMvcExceptionHandlerTestController springMvcExceptionHandlerTestController() {
      return new SpringMvcExceptionHandlerTestController()
    }

    @Bean
    ResponseFormattingSpringMvcExceptionHandlerControllerAdvice responseFormattingSpringMvcExceptionHandlerControllerAdvice() {
      return new ResponseFormattingSpringMvcExceptionHandlerControllerAdvice()
    }

    @Bean
    MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
      messageSource.defaultEncoding = "UTF-8"
      messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")
      return messageSource
    }
  }

  @Autowired
  WebApplicationContext webApplicationContext

  JsonSlurper jsonSlurper
  MockMvc mockMvc

  void setup() {
    jsonSlurper = new JsonSlurper()
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
  }

  void "should work for warning caused by HttpRequestMethodNotSupportedException"() {
    when:
    MvcResult mvcResult = mockMvc.perform(put("/test/springMvcExceptionHandlerTestControllerMethod").accept(MediaType.APPLICATION_JSON)).andReturn()

    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map
    Map metadataMap = bodyMap.metaData as Map
    Map payloadMap = bodyMap.payload as Map

    then:
    mvcResult.response.status == HttpStatus.METHOD_NOT_ALLOWED.value()
    payloadMap.size() == 0

    verifyAll(metadataMap.general as Map) {
      it.size() == 3
      timestamp
      severity == Severity.WARNING.name().toLowerCase()
      locale == new Locale("en").toString()
    }

    verifyAll(metadataMap.http as Map) {
      it.size() == 2
      status == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
      message == HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase
    }

    verifyAll(metadataMap.violation as Map) {
      it.size() == 5
      logUuid == null
      code == HttpStatus.METHOD_NOT_ALLOWED.value().toString()
      message == "Request is not valid."
      type == ViolationType.INFRASTRUCTURE_WEB.name().toLowerCase()
      validationReport == null
    }
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should work and be logged for error caused by MissingPathVariableException"() {
    given:
    TestLogger logger = TestLoggerFactory.getTestLogger(ResponseFormattingSpringMvcExceptionHandler)

    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/springMvcExceptionHandlerInvalidTestControllerMethod").accept(MediaType.APPLICATION_JSON)).andReturn()

    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map
    Map metadataMap = bodyMap.metaData as Map
    Map payloadMap = bodyMap.payload as Map

    List<LoggingEvent> loggingEventList = logger.loggingEvents.findAll { it.creatingLogger.name == ResponseFormattingSpringMvcExceptionHandler.name && it.level == Level.ERROR }
    LoggingEvent springMvcExceptionLoggingEvent = loggingEventList[0]

    then:
    loggingEventList.size() == 1
    springMvcExceptionLoggingEvent.level == Level.ERROR
    springMvcExceptionLoggingEvent.message.contains("uuid")
    springMvcExceptionLoggingEvent.message.contains(MissingPathVariableException.name)
    springMvcExceptionLoggingEvent.throwable.get().getClass() == MissingPathVariableException

    mvcResult.response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    payloadMap.size() == 0

    verifyAll(metadataMap.general as Map) {
      it.size() == 3
      timestamp
      severity == Severity.ERROR.name().toLowerCase()
      locale == new Locale("en").toString()
    }

    verifyAll(metadataMap.http as Map) {
      it.size() == 2
      status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    verifyAll(metadataMap.violation as Map) {
      it.size() == 5
      logUuid
      code == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      message == "Internal server error."
      type == ViolationType.INFRASTRUCTURE_WEB.name().toLowerCase()
      validationReport == null
    }
  }
}
