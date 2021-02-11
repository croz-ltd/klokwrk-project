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
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.klokwrk.lib.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod

import java.time.Instant

/**
 * Handles shaping and internationalization of the body in HTTP responses when execution of controller results in throwing a {@link DomainException}.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated "<code>metaData</code>" and empty "<code>payload</code>" properties. Here is an
 * example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "WARNING",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-26T09:41:04.917666Z"
 *     },
 *     "http": {
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "violation": {
 *       "code": "400",
 *       "codeMessage": "Destination location cannot accept cargo from specified origin location.",
 *       "type": "DOMAIN"
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Here, "<code>violation.codeMessage</code>" entry is internationalized.
 * <p/>
 * When used from Spring Boot application, the easiest is to create a controller advice that is eligible for component scanning (&#64;ControllerAdvice is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingDomainExceptionHandlerControllerAdvice extends ResponseFormattingDomainExceptionHandler {
 * }
 * </pre>
 * For internationalization of default messages, we are defining a resource bundle with base name "<code>responseFormattingDefaultMessages</code>". In Spring Boot application, that resource bundle
 * needs to be configured, for example, in <code>application.yml</code>:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 * The list of message codes which will be tried against the resource bundle is created by {@link MessageSourceResolvableHelper}. For resolving messages we are using
 * <code>httpResponseMetaData.violation.codeMessage</code> prefix for <code>propertyPath</code> property of <code>MessageSourceResolvableSpecification</code>. This is to avoid potential future
 * conflicts in resource bundle keys if we'll need message resolving over some other <code>propertyPath</code>.
 * <p/>
 * Here is a list of <code>MessageSourceResolvableSpecification</code> property values used for resolving internationalized messages:
 * <ul>
 *   <li>controllerSimpleName: simple class name (without package) of a controller that was executing when an exception occurred</li>
 *   <li>controllerMethodName: method name of a controller that was executing when an exception occurred</li>
 *   <li>messageCategory: <code>failure</code></li>
 *   <li>messageType: <code>domain</code></li>
 *   <li>messageSubType: value of <code>domainException.violationInfo.violationCode.codeAsText</code></li>
 *   <li>severity: value of <code>domainException.violationInfo.severity</code></li>
 *   <li>propertyPath: <code>httpResponseMetaData.violation.codeMessage</code></li>
 * </ul>
 *
 * @see MessageSourceResolvableHelper
 */
@CompileStatic
class ResponseFormattingDomainExceptionHandler implements MessageSourceAware {
  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleDomainException(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpResponseMetaData httpResponseMetaData = createHttpResponseMetaData(domainException, handlerMethod, locale)
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaData createHttpResponseMetaData(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: domainException.violationInfo.severity, locale: locale),
        violation: createResponseMetaDataViolationPart(domainException),
        http: createHttpResponseMetaDataPart(httpStatus)
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, domainException, handlerMethod, locale)

    return httpResponseMetaData
  }

  protected HttpStatus mapDomainExceptionToHttpStatus(DomainException domainException) {
    HttpStatus httpStatus
    switch (domainException.violationInfo.violationCode.code) {
      case ViolationCode.BAD_REQUEST.code:
        httpStatus = HttpStatus.BAD_REQUEST
        break
      case ViolationCode.NOT_FOUND.code:
        httpStatus = HttpStatus.NOT_FOUND
        break
      default:
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        break
    }

    return httpStatus
  }

  protected ResponseMetaDataViolationPart createResponseMetaDataViolationPart(DomainException domainException) {
    ResponseMetaDataViolationPart responseMetaDataViolationPart = new ResponseMetaDataViolationPart(
        code: domainException.violationInfo.violationCode.code,
        codeMessage: domainException.violationInfo.violationCode.codeMessage,
        type: ViolationType.DOMAIN
    )

    return responseMetaDataViolationPart
  }

  protected HttpResponseMetaDataHttpPart createHttpResponseMetaDataPart(HttpStatus httpStatus) {
    HttpResponseMetaDataHttpPart httpResponseMetaDataPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)
    return httpResponseMetaDataPart
  }

  protected HttpResponseMetaData localizeHttpResponseMetaData(
      HttpResponseMetaData httpResponseMetaData, DomainException domainException, HandlerMethod handlerMethod, Locale locale)
  {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: "domain",
        messageSubType: domainException.violationInfo.violationCode.codeAsText,
        severity: domainException.violationInfo.severity.toString().toLowerCase()
    )

    resolvableMessageSpecification.propertyPath = "httpResponseMetaData.violation.codeMessage"
    httpResponseMetaData.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaData
  }
}
