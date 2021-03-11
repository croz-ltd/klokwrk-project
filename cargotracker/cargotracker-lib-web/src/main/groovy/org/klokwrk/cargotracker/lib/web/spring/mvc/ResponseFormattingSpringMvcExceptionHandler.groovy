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

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.klokwrk.lib.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.Nullable
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.WebUtils

import java.time.Instant

/**
 * Handles shaping and internationalizing the body in HTTP responses when the execution of request results in throwing SpringMvc specific exceptions.
 * <p/>
 * SpringMvc exceptions taken into account are those that are handled by SpringMvc own {@link ResponseEntityExceptionHandler}, which is also the parent of this class.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated {@code metaData} and empty {@code payload} properties. Here is an example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "warning",
 *       "locale": "en_GB",
 *       "timestamp": "2021-02-09T10:09:36.354151Z"
 *     },
 *     "http": {
 *       "status": "405",
 *       "message": "Method Not Allowed"
 *     },
 *     "violation": {
 *       "code": "405",
 *       "codeMessage": "Request is not valid.",
 *       "type": "other"
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * For {@code error} severity, the response body also contains {@code metaData.violation.logUuid} property with the value of generated UUID. That UUID is part of the message logged for the exception.
 * For {@code warning} severity, there is no logging of the exception.
 * <p/>
 * In the response above, property {@code metaData.violation.codeMessage} needs to be localized.
 * <p/>
 * When used from the Spring Boot application, the easiest is to create controller advice that is eligible for component scanning (&#64;ControllerAdvice annotation is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingSpringMvcExceptionHandlerControllerAdvice extends ResponseFormattingSpringMvcExceptionHandler {
 * }
 * </pre>
 * For localization purposes, we are defining {@code responseFormattingDefaultMessages} resource bundle containing default messages. In the Spring Boot application, that resource bundle needs to be
 * configured, for example, in {@code application.yml} file:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 * Localization message codes for {@code metaData.violation.codeMessage} property is created with
 * {@link MessageSourceResolvableHelper#createMessageCodeListForViolationCodeMessageOfOtherFailure(org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification)} method, where you can look
 * for further details.
 *
 * @see MessageSourceResolvableHelper
 * @see MessageSourceResolvableHelper#createMessageCodeListForViolationCodeMessageOfOtherFailure(org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification)
 */
@CompileStatic
class ResponseFormattingSpringMvcExceptionHandler extends ResponseEntityExceptionHandler implements MessageSourceAware {
  static private final Logger log = LoggerFactory.getLogger(ResponseFormattingSpringMvcExceptionHandler)

  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @SuppressWarnings("Instanceof")
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception springMvcException, @Nullable Object body, HttpHeaders httpHeaders, HttpStatus httpStatus, WebRequest webRequest) {
    if (HttpStatus.INTERNAL_SERVER_ERROR == httpStatus) {
      webRequest.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, springMvcException, WebRequest.SCOPE_REQUEST)
    }

    String logUuid = null
    if (httpStatus.is5xxServerError()) {
      logUuid = UUID.randomUUID()
      log.error("SpringMvc exception occured [uuid: ${ logUuid }, SpringMvcExceptionClass: ${ springMvcException.getClass().name }]", springMvcException)
    }

    Locale locale = webRequest.locale
    HandlerMethod handlerMethod = null
    Object handlerMethodAttribute = webRequest.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
    if (handlerMethodAttribute instanceof HandlerMethod) {
      handlerMethod = webRequest.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) as HandlerMethod
    }

    HttpResponseMetaData httpResponseMetaData = createHttpResponseMetaData(springMvcException, handlerMethod, locale, logUuid, httpStatus)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaData createHttpResponseMetaData(Exception springMvcException, HandlerMethod handlerMethod, Locale locale, String logUuid, HttpStatus httpStatus) {
    ResponseMetaDataViolationPart responseMetaDataReportViolationPart =
        new ResponseMetaDataViolationPart(code: httpStatus.value().toString(), codeMessage: httpStatus.reasonPhrase, type: ViolationType.OTHER.name().toLowerCase(), logUuid: logUuid)

    HttpResponseMetaDataHttpPart httpResponseMetaDataHttpPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)

    Severity severity = Severity.WARNING
    if (httpStatus.is5xxServerError()) {
      severity = Severity.ERROR
    }

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: severity.name().toLowerCase(), locale: locale),
        violation: responseMetaDataReportViolationPart,
        http: httpResponseMetaDataHttpPart
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, handlerMethod, locale, springMvcException, severity)

    return httpResponseMetaData
  }

  protected HttpResponseMetaData localizeHttpResponseMetaData(HttpResponseMetaData httpResponseMetaData, HandlerMethod handlerMethod, Locale locale, Exception springMvcException, Severity severity) {
    String controllerSimpleName = "UnknownController"
    String controllerMethodName = "unknownControllerMethod"
    if (handlerMethod) {
      controllerSimpleName = handlerMethod.beanType.simpleName.uncapitalize()
      controllerMethodName = handlerMethod.method.name
    }

    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: controllerSimpleName,
        controllerMethodName: controllerMethodName,
        messageCategory: "failure",
        messageType: ViolationType.OTHER.name().toLowerCase(),
        messageSubType: springMvcException.getClass().simpleName.uncapitalize(),
        severity: severity.name().toLowerCase()
    )

    httpResponseMetaData.violation.codeMessage = MessageSourceResolvableHelper.resolveMessageCodeList(
        messageSource, MessageSourceResolvableHelper.createMessageCodeListForViolationCodeMessageOfOtherFailure(resolvableMessageSpecification), locale
    )

    return httpResponseMetaData
  }
}
