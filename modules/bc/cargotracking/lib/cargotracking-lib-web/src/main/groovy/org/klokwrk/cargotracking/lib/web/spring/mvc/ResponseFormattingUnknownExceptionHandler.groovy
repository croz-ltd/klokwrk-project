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
package org.klokwrk.cargotracking.lib.web.spring.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.lib.boundary.api.application.exception.IdentifiedRuntimeException
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracking.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracking.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.klokwrk.lib.hi.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.hi.spring.context.MessageSourceResolvableSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod

import java.time.Instant

/**
 * Handles shaping and internationalizing the body in HTTP responses when the execution of request results in throwing non-anticipated exception.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated {@code metaData} and empty {@code payload} properties. Here is an example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "error",
 *       "locale": "en_GB",
 *       "timestamp": "2021-02-09T10:09:36.354151Z"
 *     },
 *     "http": {
 *       "status": "500",
 *       "message": "Internal Server Error"
 *     },
 *     "violation": {
 *       "code": "500",
 *       "message": "Internal server error.",
 *       "type": "unknown",
 *       "logUuid": "116be9a6-9f38-4954-8b8f-e57e781655d0"
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Property {@code metaData.violation.message} needs to be localized.
 * <p/>
 * When used from the Spring Boot application, the easiest is to create controller advice that is eligible for component scanning (&#64;ControllerAdvice annotation is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingUnknownExceptionHandlerControllerAdvice extends ResponseFormattingUnknownExceptionHandler {
 * }
 * </pre>
 * For localization purposes, we are defining {@code responseFormattingDefaultMessages} resource bundle containing default messages. In the Spring Boot application, that resource bundle needs to be
 * configured, for example, in {@code application.yml} file:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 * Localization message codes for {@code metaData.violation.message} property is created with
 * {@link MessageSourceResolvableHelper#makeMessageCodeListForViolationMessageOfUnknownFailure(MessageSourceResolvableSpecification)} method, where you can look
 * for further details.
 *
 * @see MessageSourceResolvableHelper
 * @see MessageSourceResolvableHelper#makeMessageCodeListForViolationMessageOfUnknownFailure(MessageSourceResolvableSpecification)
 */
@CompileStatic
class ResponseFormattingUnknownExceptionHandler implements MessageSourceAware {
  static private final Logger log = LoggerFactory.getLogger(ResponseFormattingUnknownExceptionHandler)

  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleUnknownException(Throwable unknownException, HandlerMethod handlerMethod, Locale locale) {
    String logUuid = UUID.randomUUID()

    if (unknownException instanceof IdentifiedRuntimeException) {
      IdentifiedRuntimeException identifiedRuntimeException = unknownException as IdentifiedRuntimeException
      log.error("Unknown exception occured [uuid: ${ logUuid }, exceptionId: ${identifiedRuntimeException.exceptionId} , unknownExceptionClass: ${ unknownException.getClass().name }]", unknownException)
    }
    else {
      log.error("Unknown exception occured [uuid: ${ logUuid }, unknownExceptionClass: ${ unknownException.getClass().name }]", unknownException)
    }

    HttpResponseMetaData httpResponseMetaData = makeHttpResponseMetaData(unknownException, handlerMethod, locale, logUuid)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)

    return responseEntity
  }

  protected HttpResponseMetaData makeHttpResponseMetaData(Throwable unknownException, HandlerMethod handlerMethod, Locale locale, String logUuid) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    ResponseMetaDataViolationPart responseMetaDataReportViolationPart =
        new ResponseMetaDataViolationPart(code: httpStatus.value().toString(), message: httpStatus.reasonPhrase, type: ViolationType.UNKNOWN.name().toLowerCase(), logUuid: logUuid)

    HttpResponseMetaDataHttpPart httpResponseMetaDataHttpPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.ERROR.name().toLowerCase(), locale: locale),
        violation: responseMetaDataReportViolationPart,
        http: httpResponseMetaDataHttpPart
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, handlerMethod, locale, unknownException)

    return httpResponseMetaData
  }

  protected HttpResponseMetaData localizeHttpResponseMetaData(HttpResponseMetaData httpResponseMetaData, HandlerMethod handlerMethod, Locale locale, Throwable unknownException) {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: ViolationType.UNKNOWN.name().toLowerCase(),
        messageSubType: unknownException.getClass().simpleName.uncapitalize(),
        severity: Severity.ERROR.name().toLowerCase()
    )

    httpResponseMetaData.violation.message = MessageSourceResolvableHelper.resolveMessageCodeList(
        locale,
        messageSource,
        MessageSourceResolvableHelper.makeMessageCodeListForViolationMessageOfUnknownFailure(resolvableMessageSpecification)
    )

    return httpResponseMetaData
  }
}
