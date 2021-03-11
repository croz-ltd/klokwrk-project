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
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebAppConfiguration
@ContextConfiguration(classes = [WebConfig])
class ResponseFormattingResponseBodyAdviceSpecification extends Specification {

  @RestController
  @RequestMapping("/test")
  static class TestController {
    @PostMapping("/testControllerMethodWithOperationResponse")
    OperationResponse<Map> testControllerMethodWithOperationResponse() {
      return new OperationResponse<Map>(payload: [data: "Some testing data"])
    }

    @PostMapping("/testControllerMethodWithMapResponse")
    Map testControllerMethodWithMapResponse() {
      return [data: "Some testing data"]
    }

    @PostMapping("/testControllerMethodWithoutResponse")
    void testControllerMethodWithoutResponse() {
    }
  }

  @ControllerAdvice
  static class ResponseFormattingResponseBodyAdviceControllerAdvice extends ResponseFormattingResponseBodyAdvice {
  }

  @EnableWebMvc
  @Configuration
  static class WebConfig implements WebMvcConfigurer {
    @Bean
    TestController testController() {
      return new TestController()
    }

    @Bean
    ResponseFormattingResponseBodyAdviceControllerAdvice responseFormattingResponseBodyAdviceControllerAdvice() {
      return new ResponseFormattingResponseBodyAdviceControllerAdvice()
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
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build()
  }

  void "should format successful response as expected when result is returned from controller"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithOperationResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()

      bodyMap
      bodyMap.payload
      bodyMap.metaData

      bodyMap.payload.data == "Some testing data"

      bodyMap.metaData.general.size() == 3
      bodyMap.metaData.general.timestamp
      bodyMap.metaData.general.severity == "info"
      bodyMap.metaData.general.locale == "en"

      bodyMap.metaData.violation == null

      bodyMap.metaData.http.size() == 2
      bodyMap.metaData.http.status == "200"
      bodyMap.metaData.http.message == "OK"
    }
  }

  void "should ignore response that is not an instance of OperationResponse"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithMapResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString
    Map bodyMap = jsonSlurper.parseText(bodyText) as Map

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()

      bodyMap
      bodyMap.data == "Some testing data"
    }
  }

  void "should ignore response without body"() {
    when:
    MvcResult mvcResult = mockMvc.perform(post("/test/testControllerMethodWithoutResponse").accept(MediaType.APPLICATION_JSON)).andReturn()
    String bodyText = mvcResult.response.contentAsString

    then:
    verifyAll {
      mvcResult.response.status == HttpStatus.OK.value()
      bodyText == ""
    }
  }
}
