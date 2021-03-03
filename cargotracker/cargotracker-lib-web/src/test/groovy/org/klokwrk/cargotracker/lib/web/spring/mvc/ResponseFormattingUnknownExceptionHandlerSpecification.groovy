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

import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import uk.org.lidalia.slf4jext.Level

import java.lang.reflect.Method

class ResponseFormattingUnknownExceptionHandlerSpecification extends Specification {
  static class TestController {
    @SuppressWarnings(["unused", "ThrowRuntimeException"])
    OperationResponse<Map> testControllerMethod() {
      throw new RuntimeException("Runtime exception message.")
    }
  }

  Locale locale
  ResponseFormattingUnknownExceptionHandler responseFormattingUnknownExceptionHandler
  HandlerMethod handlerMethod

  void setup() {
    TestLoggerFactory.clearAll()
    //    TestLoggerFactory.instance.printLevel = Level.DEBUG // uncomment if you want to see logging output during the test

    locale = new Locale("en")
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.defaultEncoding = "UTF-8"
    messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")

    responseFormattingUnknownExceptionHandler = new ResponseFormattingUnknownExceptionHandler()
    responseFormattingUnknownExceptionHandler.messageSource = messageSource

    TestController testController = new TestController()
    Method testControllerMethod = TestController.declaredMethods.find({ Method method -> method.name == "testControllerMethod" })
    handlerMethod = new HandlerMethod(testController, testControllerMethod)
  }

  void cleanup() {
    TestLoggerFactory.clearAll()
  }

  void "should work as expected for various unexpected exceptions [exceptionClass: #exceptionParam.getClass().simpleName]"() {
    given:
    Throwable unknownException = exceptionParam

    when:
    ResponseEntity responseEntity = responseFormattingUnknownExceptionHandler.handleUnknownException(unknownException, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0
      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.ERROR.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 4
      metadata.violation.code == "500"
      metadata.violation.codeMessage == "Internal server error."
      metadata.violation.type == ViolationType.UNKNOWN.name().toLowerCase()
      metadata.violation.logUuid.size() == "116be9a6-9f38-4954-8b8f-e57e781655d0".size()

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    where:
    exceptionParam              | _
    new RuntimeException()      | _
    new NullPointerException()  | _
    new IllegalStateException() | _
  }

  void "should log unexpected exceptions on error level [exceptionClass: #exceptionParam.getClass().simpleName]"() {
    given:
    Throwable unknownException = exceptionParam
    TestLogger logger = TestLoggerFactory.getTestLogger(ResponseFormattingUnknownExceptionHandler)

    when:
    responseFormattingUnknownExceptionHandler.handleUnknownException(unknownException, handlerMethod, locale)
    List<LoggingEvent> loggingEventList = logger.loggingEvents.findAll { it.creatingLogger.name == ResponseFormattingUnknownExceptionHandler.name && it.level == Level.ERROR }
    LoggingEvent unknownExceptionLoggingEvent = loggingEventList[0]

    then:
    loggingEventList.size() == 1
    unknownExceptionLoggingEvent.level == Level.ERROR
    unknownExceptionLoggingEvent.message.contains("uuid")
    unknownExceptionLoggingEvent.message.contains(exceptionParam.getClass().name)
    unknownExceptionLoggingEvent.throwable.get() == exceptionParam

    where:
    exceptionParam              | _
    new RuntimeException()      | _
    new NullPointerException()  | _
    new IllegalStateException() | _
  }
}
