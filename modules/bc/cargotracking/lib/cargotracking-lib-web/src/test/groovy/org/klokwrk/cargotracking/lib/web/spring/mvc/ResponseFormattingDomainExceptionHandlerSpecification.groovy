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

import org.klokwrk.cargotracking.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.QueryException
import org.klokwrk.cargotracking.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationCode
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import java.lang.reflect.Field
import java.lang.reflect.Method

class ResponseFormattingDomainExceptionHandlerSpecification extends Specification {
  static class TestController {
    @SuppressWarnings("unused")
    OperationResponse<Map> testControllerMethod() {
      return new OperationResponse<Map>(payload: [someData: "Testing data"])
    }
  }

  Locale locale
  ResponseFormattingDomainExceptionHandler responseFormattingDomainExceptionHandler
  HandlerMethod handlerMethod

  void setup() {
    locale = new Locale("en")
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.defaultEncoding = "UTF-8"
    messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")

    responseFormattingDomainExceptionHandler = new ResponseFormattingDomainExceptionHandler()
    responseFormattingDomainExceptionHandler.messageSource = messageSource

    TestController testController = new TestController()
    Method testControllerMethod = TestController.declaredMethods.find({ Method method -> method.name == "testControllerMethod" })
    handlerMethod = new HandlerMethod(testController, testControllerMethod)
  }

  void "should work as expected for default domain exceptions [exceptionClass: #exceptionParam.getClass().simpleName]"() {
    given:
    DomainException exception = exceptionParam

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

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
      metadata.violation.message == "Error"
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    where:
    exceptionParam         | _
    new DomainException()  | _
    new CommandException() | _
    new QueryException()   | _
  }

  private String findViolationInfoConstantName(ViolationInfo violationInfo) {
    Field[] fieldList = violationInfo.getClass().declaredFields
    Field foundField = fieldList
        .each({ Field field -> field.accessible = true })
        .find({ Field field -> field.get(violationInfo) == violationInfo })

    return foundField?.name
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  void "should work as expected for existing ViolationInfo constants [violationInfoConstantName: #violationInfoConstantName]"() {
    given:
    assert ViolationInfo.declaredFields.findAll({ it.type == ViolationInfo }).size() == 3

    DomainException exception = exceptionParam

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0

      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == severityParam
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == violationCodeParam
      metadata.violation.message == messageParam
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == httpStatusParam
      metadata.http.message == httpMessageParam
    }

    where:
    violationInfoParam        | exceptionParam                           | severityParam                         | messageParam | violationCodeParam | httpStatusParam | httpMessageParam
    ViolationInfo.UNKNOWN     | new DomainException(violationInfoParam)  | Severity.ERROR.name().toLowerCase()   | "Error"      | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.UNKNOWN     | new CommandException(violationInfoParam) | Severity.ERROR.name().toLowerCase()   | "Error"      | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.UNKNOWN     | new QueryException(violationInfoParam)   | Severity.ERROR.name().toLowerCase()   | "Error"      | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.BAD_REQUEST | new DomainException(violationInfoParam)  | Severity.WARNING.name().toLowerCase() | "Warning"    | "400"              | "400"           | "Bad Request"
    ViolationInfo.BAD_REQUEST | new CommandException(violationInfoParam) | Severity.WARNING.name().toLowerCase() | "Warning"    | "400"              | "400"           | "Bad Request"
    ViolationInfo.BAD_REQUEST | new QueryException(violationInfoParam)   | Severity.WARNING.name().toLowerCase() | "Warning"    | "400"              | "400"           | "Bad Request"
    ViolationInfo.NOT_FOUND   | new DomainException(violationInfoParam)  | Severity.WARNING.name().toLowerCase() | "Warning"    | "404"              | "404"           | "Not Found"
    ViolationInfo.NOT_FOUND   | new CommandException(violationInfoParam) | Severity.WARNING.name().toLowerCase() | "Warning"    | "404"              | "404"           | "Not Found"
    ViolationInfo.NOT_FOUND   | new QueryException(violationInfoParam)   | Severity.WARNING.name().toLowerCase() | "Warning"    | "404"              | "404"           | "Not Found"

    violationInfoConstantName = findViolationInfoConstantName(violationInfoParam)
  }

  void "should work for DomainException with message"() {
    given:
    ViolationCode violationCode = ViolationCode.make("12345", "codeMessage")
    ViolationInfo violationInfo = new ViolationInfo(severity: Severity.WARNING, violationCode: violationCode)
    DomainException exception = new DomainException(violationInfo, "Domain exception message")

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0

      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.WARNING.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "12345"
      metadata.violation.message == "Domain exception message"
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }
  }

  void "should work for DomainException with default message and custom ViolationInfo without resolvableMessageKey"() {
    given:
    ViolationCode violationCode = ViolationCode.make("12345", "codeMessage")
    ViolationInfo violationInfo = new ViolationInfo(severity: Severity.WARNING, violationCode: violationCode)
    DomainException exception = new DomainException(violationInfo, domainExceptionMessageParam)

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0

      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.WARNING.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "12345"
      metadata.violation.message == "codeMessage"
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    where:
    domainExceptionMessageParam | _
    null                        | _
    ""                          | _
    "  "                        | _
  }

  void "should work for custom ViolationInfo with resolvableMessageKey and without resolvableMessageParameters"() {
    given:
    ViolationCode violationCode = ViolationCode.make("12345", "codeMessage", resolvableMessageKeyParam)
    ViolationInfo violationInfo = new ViolationInfo(severity: Severity.WARNING, violationCode: violationCode)
    DomainException exception = new DomainException(violationInfo)

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0

      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.WARNING.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "12345"
      metadata.violation.message == violationMessageParam
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    where:
    resolvableMessageKeyParam               | violationMessageParam
    "myTestCode"                            | "My violation code message"
    "myMainTestCode.myMoreSpecificTestCode" | "My more specific violation code message"
  }

  void "should work for custom ViolationInfo with resolvableMessageKey and resolvableMessageParameters"() {
    given:
    ViolationCode violationCode = ViolationCode.make("12345", "codeMessage", resolvableMessageKeyParam, resolvableMessageParametersParam)
    ViolationInfo violationInfo = new ViolationInfo(severity: Severity.WARNING, violationCode: violationCode)
    DomainException exception = new DomainException(violationInfo)

    when:
    ResponseEntity responseEntity = responseFormattingDomainExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0

      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.WARNING.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "12345"
      metadata.violation.message == violationMessageParam
      metadata.violation.type == ViolationType.DOMAIN.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport == null

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }

    where:
    resolvableMessageKeyParam                         | resolvableMessageParametersParam | violationMessageParam
    "myTestCodeWithParams"                            | ["paramOne", "paramTwo"]         | "My violation code message with params - paramOne, paramTwo"
    "myMainTestCode.myMoreSpecificTestCodeWithParams" | ["paramOne", "paramTwo"]         | "My more specific violation code message - paramOne, paramTwo"
  }
}
