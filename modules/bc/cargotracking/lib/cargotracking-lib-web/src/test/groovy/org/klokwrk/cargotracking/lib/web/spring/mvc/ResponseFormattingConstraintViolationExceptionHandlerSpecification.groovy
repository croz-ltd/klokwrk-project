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

import org.hibernate.validator.constraints.ScriptAssert
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ResponseMetaDataValidationReportPart
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ValidationReportConstraintViolation
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.lang.reflect.Method
import java.time.LocalDateTime

class ResponseFormattingConstraintViolationExceptionHandlerSpecification extends Specification {
  static class TestController {
    @SuppressWarnings("unused")
    OperationResponse<Map> testControllerMethod() {
      return new OperationResponse<Map>(payload: [someData: "Testing data"])
    }
  }

  static class TestRootObject {
    @NotNull
    @Size(min = 1, max = 15)
    String stringProperty

    @NotNull
    @Past
    LocalDateTime localDateTimeProperty

    @Valid
    @NotNull
    NestedTestObject nestedTestObject
  }

  @ScriptAssert(lang = "groovy", script = "!_this.nestedStringProperty.endsWith('bla')")
  static class NestedTestObject {
    @NotBlank
    @Size(min = 1, max = 15)
    String nestedStringProperty

    Integer nestedIntegerProperty

    @SuppressWarnings("unused")
    @NotNull
    @Min(value = 10L, message = "") // Attribute message is empty string to test that scenario too. Do note that compiler will complain if message is null, thus we do not test for that case.
    Integer getNestedIntegerProperty() {
      return nestedIntegerProperty
    }

    @SuppressWarnings("unused")
    @NotNull
    @Min(value = 20L, message = "   ") // Attribute message is whitespace string to test that scenario too
    Integer secondNestedIntegerProperty
  }

  Locale locale
  ResponseFormattingConstraintViolationExceptionHandler responseFormattingConstraintViolationExceptionHandler
  HandlerMethod handlerMethod
  Validator validator

  void setup() {
    locale = new Locale("en")
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.defaultEncoding = "UTF-8"
    messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")

    responseFormattingConstraintViolationExceptionHandler = new ResponseFormattingConstraintViolationExceptionHandler()
    responseFormattingConstraintViolationExceptionHandler.messageSource = messageSource

    TestController testController = new TestController()
    Method testControllerMethod = TestController.declaredMethods.find({ Method method -> method.name == "testControllerMethod" })
    handlerMethod = new HandlerMethod(testController, testControllerMethod)

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()
    validator = validatorFactory.getValidator()
  }

  void "should create expected response structure for validation failure"() {
    given:
    TestRootObject testRootObject = new TestRootObject(stringProperty: "bla", localDateTimeProperty: null, nestedTestObject: null)
    Set<ConstraintViolation> constraintViolationSet = validator.validate(testRootObject)
    ConstraintViolationException constraintViolationException = new ConstraintViolationException(constraintViolationSet)

    when:
    ResponseEntity responseEntity = responseFormattingConstraintViolationExceptionHandler.handleConstraintViolationException(constraintViolationException, handlerMethod, locale)
    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    payload.size() == 0
    metadata.size() > 0

    verifyAll {
      metadata.general.propertiesFiltered.size() == 3
      metadata.general.timestamp
      metadata.general.severity == Severity.WARNING.name().toLowerCase()
      metadata.general.locale == new Locale("en")

      metadata.violation.propertiesFiltered.size() == 5
      metadata.violation.code == "400"
      metadata.violation.message == "Request is not valid."
      metadata.violation.type == ViolationType.VALIDATION.name().toLowerCase()
      metadata.violation.logUuid == null
      metadata.violation.validationReport

      metadata.http.propertiesFiltered.size() == 2
      metadata.http.status == "400"
      metadata.http.message == "Bad Request"
    }
  }

  void "should create expected validation report in case of validation failure"() {
    given:
    LocalDateTime currentLocalDateTime = LocalDateTime.now()
    NestedTestObject nestedTestObject = new NestedTestObject(nestedStringProperty: "nested-bla", nestedIntegerProperty: 1, secondNestedIntegerProperty: 2)
    TestRootObject testRootObject = new TestRootObject(stringProperty: "", localDateTimeProperty: currentLocalDateTime + 60, nestedTestObject: nestedTestObject)
    Set<ConstraintViolation> constraintViolationSet = validator.validate(testRootObject)
    ConstraintViolationException constraintViolationException = new ConstraintViolationException(constraintViolationSet)

    when:
    ResponseEntity responseEntity = responseFormattingConstraintViolationExceptionHandler.handleConstraintViolationException(constraintViolationException, handlerMethod, locale)
    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>

    ResponseMetaDataValidationReportPart responseMetaDataValidationReportPart = body.metaData.violation.validationReport as ResponseMetaDataValidationReportPart
    ValidationReportConstraintViolation stringPropertyConstraintViolation = responseMetaDataValidationReportPart.constraintViolations.find({ it.path == "stringProperty" })
    ValidationReportConstraintViolation localDateTimePropertyConstraintViolation = responseMetaDataValidationReportPart.constraintViolations.find({ it.path == "localDateTimeProperty" })
    ValidationReportConstraintViolation nestedTestObjectPropertyConstraintViolation = responseMetaDataValidationReportPart.constraintViolations.find({ it.path == "nestedTestObject" })
    ValidationReportConstraintViolation nestedIntegerPropertyConstraintViolation = responseMetaDataValidationReportPart.constraintViolations.find({ it.path == "nestedTestObject.nestedIntegerProperty" })
    ValidationReportConstraintViolation secondIntegerPropertyConstraintViolation = responseMetaDataValidationReportPart.constraintViolations.find({ it.path == "nestedTestObject.secondNestedIntegerProperty" })

    then:
    verifyAll(responseMetaDataValidationReportPart) {
      it.root.type == "testRootObject"

      it.constraintViolations.size() == 5

      stringPropertyConstraintViolation.type == "size"
      stringPropertyConstraintViolation.scope == "property"
      stringPropertyConstraintViolation.message == "size must be between 1 and 15"

      localDateTimePropertyConstraintViolation.type == "past"
      localDateTimePropertyConstraintViolation.scope == "property"
      localDateTimePropertyConstraintViolation.message == "must be a past date"

      nestedTestObjectPropertyConstraintViolation.type == "scriptAssert"
      nestedTestObjectPropertyConstraintViolation.scope == "object"
      nestedTestObjectPropertyConstraintViolation.message == /script expression "!_this.nestedStringProperty.endsWith('bla')" didn't evaluate to true/

      nestedIntegerPropertyConstraintViolation.type == "min"
      nestedIntegerPropertyConstraintViolation.scope == "property"
      nestedIntegerPropertyConstraintViolation.message == "Request is not valid." // Resolved from bundle

      secondIntegerPropertyConstraintViolation.type == "min"
      secondIntegerPropertyConstraintViolation.scope == "property"
      secondIntegerPropertyConstraintViolation.message == "Request is not valid." // Resolved from bundle
    }
  }
}
