/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.cargotracker.lib.boundary.api.metadata.report.ResponseMetaDataReportViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.web.metadata.report.HttpResponseMetaDataReport
import org.klokwrk.cargotracker.lib.web.metadata.report.HttpResponseMetaDataReportHttpPart
import org.klokwrk.lib.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

import java.time.Instant

/**
 * Handles shaping and internationalization of the body in HTTP responses when execution of controller results in throwing a {@link DomainException}.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated "<code>metaData</code>" and empty "<code>payload</code>" properties. Here is an
 * example:
 * <pre>
 * {
 *   "metaData": {
 *     "http": {
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "severity": "WARNING",
 *     "violation": {
 *       "code": "400",
 *       "codeMessage": "Destination location cannot accept cargo from specified origin location."
 *     },
 *     "locale": "en_GB",
 *     "timestamp": "2020-04-26T09:41:04.917666Z"
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
 * The list of message codes which will be tried against the resource bundle is created by {@link MessageSourceResolvableHelper}. For resolving messages we are using <code>report.</code> prefix for
 * <code>propertyPath</code> property of <code>MessageSourceResolvableSpecification</code>. This is to avoid potential future conflicts in resource bundle keys if we'll need message resolving over
 * some other <code>propertyPath</code>.
 *
 * @see MessageSourceResolvableHelper
 */
@CompileStatic
class ResponseFormattingDomainExceptionHandler extends ResponseEntityExceptionHandler implements MessageSourceAware {
  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleDomainException(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpResponseMetaDataReport httpResponseMetaDataReport = createHttpResponseReport(domainException, handlerMethod, locale)
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaDataReport.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaDataReport createHttpResponseReport(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)

    HttpResponseMetaDataReport httpResponseMetaDataReport = new HttpResponseMetaDataReport(
        timestamp: Instant.now(),
        severity: domainException.violationInfo.severity,
        locale: locale,
        violation: createResponseMetaDataReportViolationPart(domainException),
        http: createHttpResponseMetaDataReportPart(httpStatus)
    )

    httpResponseMetaDataReport = localizeHttpResponseMetaDataReport(httpResponseMetaDataReport, domainException, handlerMethod, locale)

    return httpResponseMetaDataReport
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

  protected ResponseMetaDataReportViolationPart createResponseMetaDataReportViolationPart(DomainException domainException) {
    ResponseMetaDataReportViolationPart responseMetaDataReportViolationPart = new ResponseMetaDataReportViolationPart(
        code: domainException.violationInfo.violationCode.code,
        codeMessage: domainException.violationInfo.violationCode.codeMessage
    )

    return responseMetaDataReportViolationPart
  }

  protected HttpResponseMetaDataReportHttpPart createHttpResponseMetaDataReportPart(HttpStatus httpStatus) {
    HttpResponseMetaDataReportHttpPart httpResponseMetaDataReportPart = new HttpResponseMetaDataReportHttpPart(
        status: httpStatus.value().toString(),
        message: httpStatus.reasonPhrase
    )

    return httpResponseMetaDataReportPart
  }

  protected HttpResponseMetaDataReport localizeHttpResponseMetaDataReport(
      HttpResponseMetaDataReport httpResponseMetaDataReport, DomainException domainException, HandlerMethod handlerMethod, Locale locale)
  {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: domainException.violationInfo.violationCode.codeAsText,
        messageSubType: "",
        severity: domainException.violationInfo.severity.toString().toLowerCase()
    )

    resolvableMessageSpecification.propertyPath = "report.violation.codeMessage"
    httpResponseMetaDataReport.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaDataReport
  }
}
