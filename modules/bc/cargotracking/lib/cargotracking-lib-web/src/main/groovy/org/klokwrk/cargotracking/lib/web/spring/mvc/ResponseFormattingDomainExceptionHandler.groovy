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

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationCode
import org.klokwrk.cargotracking.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracking.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.klokwrk.lib.hi.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.hi.spring.context.MessageSourceResolvableSpecification
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod

import java.time.Instant

/**
 * Handles shaping and internationalizing the body in HTTP responses when the execution of request results in throwing a {@link DomainException}.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated {@code metaData} and empty {@code payload} properties. Here is an example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "warning",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-26T09:41:04.917666Z"
 *     },
 *     "http": {
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "violation": {
 *       "code": "400",
 *       "message": "Destination location cannot accept cargo from specified origin location.",
 *       "type": "domain"
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Property {@code metaData.violation.message} is intended to be localized. For localization to work, corresponding {@code ViolationCode} property (contained in exception's {@code ViolationInfo}
 * property) has to be resolvable ({@code ViolationCode.isResolvable()} returns {@code true}). If this is not the case, hierarchical overriding of violation and exception messages is employed
 * according to the following rules:
 * <ul>
 * <li>If {@code ViolationCode.resolvableMessageKey} is available, the exception renderer should use is for resolving a message through resource bundle.</li>
 * <li>
 *   Otherwise, the exception renderer should use {@code DomainException} message directly. Note that {@code DomainException.message} is initialized to {@code ViolationCode.codeMessage} if not set
 *   explicitly to non-blank string.
 * </li>
 * </ul>
 * <p/>
 * When used from the Spring Boot application, the easiest is to create controller advice that is eligible for component scanning (&#64;ControllerAdvice annotation is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingDomainExceptionHandlerControllerAdvice extends ResponseFormattingDomainExceptionHandler {
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
 * {@link MessageSourceResolvableHelper#makeMessageCodeListForViolationMessageOfDomainFailure(MessageSourceResolvableSpecification)} method, where you can look
 * for further details.
 *
 * @see MessageSourceResolvableHelper
 * @see MessageSourceResolvableHelper#makeMessageCodeListForViolationMessageOfDomainFailure(MessageSourceResolvableSpecification)
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
    HttpResponseMetaData httpResponseMetaData = makeHttpResponseMetaData(domainException, handlerMethod, locale)
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaData makeHttpResponseMetaData(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: domainException.violationInfo.severity.name().toLowerCase(), locale: locale),
        violation: makeResponseMetaDataViolationPart(domainException, handlerMethod, locale),
        http: makeHttpResponseMetaDataPart(httpStatus)
    )

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

  protected ResponseMetaDataViolationPart makeResponseMetaDataViolationPart(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    ResponseMetaDataViolationPart responseMetaDataViolationPart = new ResponseMetaDataViolationPart(
        code: domainException.violationInfo.violationCode.code,
        message: makeResponseMetaDataViolationMessage(domainException, handlerMethod, locale),
        type: ViolationType.DOMAIN.name().toLowerCase()
    )

    return responseMetaDataViolationPart
  }

  protected String makeResponseMetaDataViolationMessage(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    String responseMetaDataViolationMessage
    if (domainException.violationInfo.violationCode.isResolvable()) {
      responseMetaDataViolationMessage = localizeHttpResponseMetaDataViolationMessage(domainException, handlerMethod, locale)
    }
    else {
      // NOTE: domain exception always has a message, which is ensured by its constructor
      responseMetaDataViolationMessage = domainException.message
    }

    return responseMetaDataViolationMessage
  }

  protected HttpResponseMetaDataHttpPart makeHttpResponseMetaDataPart(HttpStatus httpStatus) {
    HttpResponseMetaDataHttpPart httpResponseMetaDataPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)
    return httpResponseMetaDataPart
  }

  protected String localizeHttpResponseMetaDataViolationMessage(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    String fullMessageSubType = domainException.violationInfo.violationCode.resolvableMessageKey
    List<String> fullMessageSubTypeTokens = fullMessageSubType.tokenize(".")
    String messageSubTypeMain = fullMessageSubTypeTokens[0]
    String messageSubTypeDetails = fullMessageSubTypeTokens.size() > 1 ? fullMessageSubTypeTokens[1..-1].join(".") : ""

    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: ViolationType.DOMAIN.name().toLowerCase(),
        messageSubType: messageSubTypeMain,
        messageSubTypeDetails: messageSubTypeDetails,
        severity: domainException.violationInfo.severity.name().toLowerCase()
    )

    String httpResponseMetaDataViolationMessage = MessageSourceResolvableHelper.resolveMessageCodeList(
        locale,
        messageSource,
        MessageSourceResolvableHelper.makeMessageCodeListForViolationMessageOfDomainFailure(resolvableMessageSpecification),
        domainException.violationInfo.violationCode.resolvableMessageParameters
    )

    return httpResponseMetaDataViolationMessage
  }
}
