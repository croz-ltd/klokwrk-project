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
package org.klokwrk.cargotracking.lib.web.spring.mvc

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.klokwrk.cargotracking.lib.boundary.api.application.exception.RemoteHandlerException
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.slf4j.LoggerFactory
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import java.lang.reflect.Method

class ResponseFormattingUnknownExceptionHandlerSpecification extends Specification {
  static class TestController {
    @SuppressWarnings(["unused", "CodeNarc.ThrowRuntimeException"])
    OperationResponse<Map> testControllerMethod() {
      throw new RuntimeException("Runtime exception message.")
    }
  }

  Locale locale
  ResponseFormattingUnknownExceptionHandler responseFormattingUnknownExceptionHandler
  HandlerMethod handlerMethod

  void setup() {
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

  private List configureLoggerAndListAppender() {
    Logger logger = LoggerFactory.getLogger(ResponseFormattingUnknownExceptionHandler.name) as Logger
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>()
    listAppender.start()
    logger.addAppender(listAppender)

    return [logger, listAppender]
  }

  private void cleanupLogger(Logger logger, ListAppender listAppender) {
    logger.detachAppender(listAppender)
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

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "500"
      metadata.violation.message == "Internal server error."
      metadata.violation.type == ViolationType.UNKNOWN.name().toLowerCase()
      metadata.violation.logUuid.size() == "116be9a6-9f38-4954-8b8f-e57e781655d0".size()
      metadata.violation.validationReport == null

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
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()

    when:
    responseFormattingUnknownExceptionHandler.handleUnknownException(unknownException, handlerMethod, locale)

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.ERROR
      message.contains("uuid: ")
      !message.contains("exceptionId: ")
      message.contains(exceptionParam.getClass().name)
    }

    cleanup:
    cleanupLogger(logger, listAppender)

    where:
    exceptionParam              | _
    new RuntimeException()      | _
    new NullPointerException()  | _
    new IllegalStateException() | _
  }

  void "should log exception id of identified runtime exceptions on error level"() {
    given:
    String exceptionId = UUID.randomUUID()
    Throwable unknownException = new RemoteHandlerException(exceptionId, "some remote handler exception")
    def (Logger logger, ListAppender listAppender) = configureLoggerAndListAppender()

    when:
    responseFormattingUnknownExceptionHandler.handleUnknownException(unknownException, handlerMethod, locale)

    then:
    listAppender.list.size() == 1
    verifyAll(listAppender.list[0]) {
      level == Level.ERROR
      message.contains("uuid: ")
      message.contains("exceptionId: ")
      message.contains(exceptionId)
      message.contains(unknownException.getClass().name)
    }

    cleanup:
    cleanupLogger(logger, listAppender)
  }
}
