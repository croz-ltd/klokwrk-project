package org.klokwrk.cargotracker.lib.web.spring.mvc

import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.exception.QueryException
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationCode
import org.klokwrk.cargotracker.lib.boundary.api.exceptional.violation.ViolationInfo
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Method

class ResponseFormattingExceptionHandlerSpecification extends Specification {
  @SuppressWarnings("Indentation")
  static class TestController {
    @SuppressWarnings("unused")
    OperationResponse<Map> testControllerMethod() {
      return new OperationResponse<Map>(payload: [someData: "Testing data"])
    }
  }

  Locale locale
  ResponseFormattingExceptionHandler responseFormattingExceptionHandler
  HandlerMethod handlerMethod

  void setup() {
    locale = new Locale("en")
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource()
    messageSource.defaultEncoding = "UTF-8"
    messageSource.setBasenames("responseFormattingDefaultMessages", "responseFormattingTestMessages")

    responseFormattingExceptionHandler = new ResponseFormattingExceptionHandler()
    responseFormattingExceptionHandler.messageSource = messageSource

    TestController testController = new TestController()
    Method testControllerMethod = TestController.declaredMethods.find({ Method method -> method.name == "testControllerMethod" })
    handlerMethod = new HandlerMethod(testController, testControllerMethod)
  }

  @Unroll
  void "should work as expected for default domain exceptions [exceptionClass: #exceptionParam.getClass().simpleName]"() {
    given:
    DomainException exception = exceptionParam

    when:
    ResponseEntity responseEntity = responseFormattingExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0
      metadata.timestamp
      metadata.severity == Severity.ERROR
      metadata.locale == new Locale("en")
      metadata.titleText == "Error"
      metadata.titleDetailedText == "Error"
      metadata.violation.code == "500"
      metadata.violation.codeMessage == "Error"
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
    Field foundField = fieldList.find { Field field ->
      field.accessible = true
      field.get(violationInfo) == violationInfo
    }

    return foundField?.name
  }

  @Unroll
  void "should work as expected for existing ViolationInfo constants [violationInfoConstantName: #violationInfoConstantName]"() {
    given:
    assert ViolationInfo.declaredFields.findAll({ it.type == ViolationInfo }).size() == 3

    DomainException exception = exceptionParam

    when:
    ResponseEntity responseEntity = responseFormattingExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0
      metadata.timestamp
      metadata.severity == severityParam
      metadata.locale == new Locale("en")
      metadata.titleText == titleParam
      metadata.titleDetailedText == titleParam
      metadata.violation.code == violationCodeParam
      metadata.violation.codeMessage == titleParam
      metadata.http.status == httpStatusParam
      metadata.http.message == httpMessageParam
    }

    where:
    violationInfoParam        | exceptionParam                           | severityParam    | titleParam | violationCodeParam | httpStatusParam | httpMessageParam
    ViolationInfo.UNKNOWN     | new DomainException(violationInfoParam)  | Severity.ERROR   | "Error"    | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.UNKNOWN     | new CommandException(violationInfoParam) | Severity.ERROR   | "Error"    | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.UNKNOWN     | new QueryException(violationInfoParam)   | Severity.ERROR   | "Error"    | "500"              | "500"           | "Internal Server Error"
    ViolationInfo.BAD_REQUEST | new DomainException(violationInfoParam)  | Severity.WARNING | "Warning"  | "400"              | "400"           | "Bad Request"
    ViolationInfo.BAD_REQUEST | new CommandException(violationInfoParam) | Severity.WARNING | "Warning"  | "400"              | "400"           | "Bad Request"
    ViolationInfo.BAD_REQUEST | new QueryException(violationInfoParam)   | Severity.WARNING | "Warning"  | "400"              | "400"           | "Bad Request"
    ViolationInfo.NOT_FOUND   | new DomainException(violationInfoParam)  | Severity.WARNING | "Warning"  | "404"              | "404"           | "Not Found"
    ViolationInfo.NOT_FOUND   | new CommandException(violationInfoParam) | Severity.WARNING | "Warning"  | "404"              | "404"           | "Not Found"
    ViolationInfo.NOT_FOUND   | new QueryException(violationInfoParam)   | Severity.WARNING | "Warning"  | "404"              | "404"           | "Not Found"

    violationInfoConstantName = findViolationInfoConstantName(violationInfoParam)
  }

  void "should work for custom ViolationInfo"() {
    given:
    ViolationCode violationCode = new ViolationCode(code: "12345", codeAsText: "myTestCode", codeMessage: "codeMessage")
    ViolationInfo violationInfo = new ViolationInfo(severity: Severity.WARNING, violationCode: violationCode)
    DomainException exception = new DomainException(violationInfo)

    when:
    ResponseEntity responseEntity = responseFormattingExceptionHandler.handleDomainException(exception, handlerMethod, locale)

    OperationResponse<Map> body = responseEntity.body as OperationResponse<Map>
    Map metadata = body.metaData
    Map payload = body.payload

    then:
    verifyAll {
      body
      payload.size() == 0
      metadata.timestamp
      metadata.severity == Severity.WARNING
      metadata.locale == new Locale("en")
      metadata.titleText == "My warning report title text"
      metadata.titleDetailedText == "My warning report title detailed text."
      metadata.violation.code == "12345"
      metadata.violation.codeMessage == "Warning"
      metadata.http.status == HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
      metadata.http.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    }
  }
}
