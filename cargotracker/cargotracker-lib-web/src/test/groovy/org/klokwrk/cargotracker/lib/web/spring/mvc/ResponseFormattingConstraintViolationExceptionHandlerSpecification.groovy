package org.klokwrk.cargotracker.lib.web.spring.mvc

import org.hibernate.validator.constraints.ScriptAssert
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataValidationReportPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ValidationReportConstraintViolation
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past
import javax.validation.constraints.Size
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

  @ScriptAssert(lang = "javascript", script = "!_this.nestedStringProperty.endsWith('bla')")
  static class NestedTestObject {
    @NotBlank
    @Size(min = 1, max = 15)
    String nestedStringProperty

    @NotNull
    @Min(value = 10L)
    Integer nestedIntegerProperty
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
      metadata.violation.codeMessage == "Request is not valid."
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
    NestedTestObject nestedTestObject = new NestedTestObject(nestedStringProperty: "nested-bla", nestedIntegerProperty: 1)
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

    then:
    verifyAll(responseMetaDataValidationReportPart) {
      it.root.message == "Request is not valid."
      it.root.type == "testRootObject"

      it.constraintViolations.size() == 4

      stringPropertyConstraintViolation.type == "size"
      stringPropertyConstraintViolation.scope == "property"
      stringPropertyConstraintViolation.message == "size must be between 1 and 15"
      stringPropertyConstraintViolation.invalidPropertyValue == ""

      localDateTimePropertyConstraintViolation.type == "past"
      localDateTimePropertyConstraintViolation.scope == "property"
      localDateTimePropertyConstraintViolation.message == "must be a past date"
      localDateTimePropertyConstraintViolation.invalidPropertyValue == (currentLocalDateTime + 60).toString()

      nestedTestObjectPropertyConstraintViolation.type == "scriptAssert"
      nestedTestObjectPropertyConstraintViolation.scope == "object"
      nestedTestObjectPropertyConstraintViolation.message == /script expression "!_this.nestedStringProperty.endsWith('bla')" didn't evaluate to true/
      nestedTestObjectPropertyConstraintViolation.invalidPropertyValue == null // because of object scope

      nestedIntegerPropertyConstraintViolation.type == "min"
      nestedIntegerPropertyConstraintViolation.scope == "property"
      nestedIntegerPropertyConstraintViolation.message == "must be greater than or equal to 10"
      nestedIntegerPropertyConstraintViolation.invalidPropertyValue == "1"
    }
  }
}
