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
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl
import org.hibernate.validator.internal.metadata.location.ConstraintLocation
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataValidationReportPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ValidationReportConstraintViolation
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ValidationReportRoot
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
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

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import java.time.Instant

/**
 * Handles shaping and internationalizing the body in HTTP responses when the execution of request results in failing validation, a.k.a. throwing a {@link ConstraintViolationException}.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated {@code metaData} and empty {@code payload} properties. Here is an example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "timestamp": "2021-03-03T18:30:03.635859Z",
 *       "severity": "warning",
 *       "locale": "en"
 *     }
 *     "http": {
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "violation": {
 *       "code": "400",
 *       "message": "Request is not valid.",
 *       "type": "validation",
 *       "validationReport": {
 *         "root": { "type": "bookCargoRequest" },
 *         "constraintViolations": [
 *           { "type": "notNull", "scope": "property", "path": "destinationLocation", "message": "must not be null", "invalidPropertyValue": "null" },
 *           { "type": "notNull", "scope": "property", "path": "originLocation", "message": "must not be null", "invalidPropertyValue": "null" }
 *         ]
 *       }
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Following properties need to be localized: {@code metaData.violation.message} and {@code metaData.violation.validationReport.constraintViolations[].message}.
 * <p/>
 * It is important to realize that during localization of {@code metaData.violation.message}, {@code metaData.violation.validationReport.root.type} is included as a {@code messageSubType}. That way,
 * if needed, {@code metaData.violation.validationReport.root.type} can have an influence on resolved message.
 * <p/>
 * Message codes for these properties are created with utility methods from {@link MessageSourceResolvableHelper}. Look there for more details.
 * <p/>
 * When used from the Spring Boot application, the easiest is to create controller advice that is eligible for component scanning (&#64;ControllerAdvice annotation is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingConstraintViolationExceptionHandlerControllerAdvice extends ResponseFormattingConstraintViolationExceptionHandler {
 * }
 * </pre>
 * For localization purposes, we are defining {@code responseFormattingDefaultMessages} resource bundle containing default messages. In the Spring Boot application, that resource bundle needs to be
 * configured, for example, in {@code application.yml} file:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 *
 * @see MessageSourceResolvableHelper
 */
@CompileStatic
class ResponseFormattingConstraintViolationExceptionHandler implements MessageSourceAware {
  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleConstraintViolationException(ConstraintViolationException constraintViolationException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST

    HttpResponseMetaData httpResponseMetaData = createHttpResponseMetaData(constraintViolationException, handlerMethod, locale)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaData createHttpResponseMetaData(ConstraintViolationException constraintViolationException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST

    ResponseMetaDataViolationPart responseMetaDataReportViolationPart = new ResponseMetaDataViolationPart(
        code: httpStatus.value().toString(), message: httpStatus.reasonPhrase, type: ViolationType.VALIDATION.name().toLowerCase(),
        validationReport: createResponseMetaDataValidationReportPart(constraintViolationException)
    )

    HttpResponseMetaDataHttpPart httpResponseMetaDataHttpPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.WARNING.name().toLowerCase(), locale: locale),
        violation: responseMetaDataReportViolationPart,
        http: httpResponseMetaDataHttpPart
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, handlerMethod, locale, constraintViolationException)

    return httpResponseMetaData
  }

  protected ResponseMetaDataValidationReportPart createResponseMetaDataValidationReportPart(ConstraintViolationException constraintViolationException) {
    ValidationReportRoot validationReportRoot = new ValidationReportRoot(type: constraintViolationException.constraintViolations[0].rootBeanClass.simpleName.uncapitalize())

    List<ValidationReportConstraintViolation> validationReportConstraintViolationList = constraintViolationException.constraintViolations.collect({ ConstraintViolation constraintViolation ->
      String type = (constraintViolation.constraintDescriptor as ConstraintDescriptorImpl).annotationType.simpleName.uncapitalize()

      String scope = null

      ConstraintLocation.ConstraintLocationKind constraintLocationKind = (constraintViolation.constraintDescriptor as ConstraintDescriptorImpl).constraintLocationKind
      if (constraintLocationKind in [ConstraintLocation.ConstraintLocationKind.FIELD, ConstraintLocation.ConstraintLocationKind.GETTER]) {
        scope = "property"
      }

      if (constraintLocationKind == ConstraintLocation.ConstraintLocationKind.TYPE) {
        scope = "object"
      }

      String path = constraintViolation.propertyPath
      String message = constraintViolation.message?.trim() ?: null

      return new ValidationReportConstraintViolation(type: type, scope: scope, path: path, message: message)
    })

    return new ResponseMetaDataValidationReportPart(root: validationReportRoot, constraintViolations: validationReportConstraintViolationList)
  }

  @SuppressWarnings(["CodeNarc.DuplicateStringLiteral", "CodeNarc.AbcMetric"])
  protected HttpResponseMetaData localizeHttpResponseMetaData(
      HttpResponseMetaData httpResponseMetaData, HandlerMethod handlerMethod, Locale locale, ConstraintViolationException constraintViolationException)
  {
    String rootBeanType = constraintViolationException.constraintViolations[0].rootBeanClass.simpleName.uncapitalize()
    MessageSourceResolvableSpecification resolvableMessageSpecificationForViolationMessage = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: ViolationType.VALIDATION.name().toLowerCase(),
        messageSubType: rootBeanType,
        severity: Severity.WARNING.name().toLowerCase()
    )

    httpResponseMetaData.violation.message = MessageSourceResolvableHelper.resolveMessageCodeList(
        messageSource, MessageSourceResolvableHelper.createMessageCodeListForViolationMessageOfValidationFailure(resolvableMessageSpecificationForViolationMessage), locale
    )

    // constraintList
    httpResponseMetaData.violation.validationReport.constraintViolations.each { ValidationReportConstraintViolation validationReportConstraintViolation ->
      MessageSourceResolvableSpecification resolvableMessageSpecificationForConstraintViolation = new MessageSourceResolvableSpecification(
          controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
          controllerMethodName: handlerMethod.method.name,
          messageCategory: "failure",
          messageType: ViolationType.VALIDATION.name().toLowerCase(),
          messageSubType: rootBeanType,
          severity: Severity.WARNING.name().toLowerCase(),
          constraintViolationPropertyPath: validationReportConstraintViolation.path,
          constraintViolationType: validationReportConstraintViolation.type
      )

      validationReportConstraintViolation.message = MessageSourceResolvableHelper.resolveMessageCodeList(
          messageSource,
          MessageSourceResolvableHelper.createMessageCodeListForConstraintViolationMessageOfValidationFailure(resolvableMessageSpecificationForConstraintViolation, validationReportConstraintViolation.message),
          locale,
          validationReportConstraintViolation.message
      )
    }

    return httpResponseMetaData
  }
}
