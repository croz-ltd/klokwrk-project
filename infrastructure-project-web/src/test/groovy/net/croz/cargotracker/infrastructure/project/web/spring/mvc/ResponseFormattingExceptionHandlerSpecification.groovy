package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.DomainException
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.Severity
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import java.lang.reflect.Method

class ResponseFormattingExceptionHandlerSpecification extends Specification {
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
    messageSource.setDefaultEncoding("UTF-8")
    messageSource.setBasename("responseFormattingDefaultMessages")

    responseFormattingExceptionHandler = new ResponseFormattingExceptionHandler()
    responseFormattingExceptionHandler.setMessageSource(messageSource)

    TestController testController = new TestController()
    Method testControllerMethod = TestController.declaredMethods.find({ Method method -> method.name == "testControllerMethod" })
    handlerMethod = new HandlerMethod(testController, testControllerMethod)
  }

  @SuppressWarnings("GrUnresolvedAccess")
  void "should work as expected for default DomainException"() {
    given:
    DomainException domainException = new DomainException()

    when:
    ResponseEntity responseEntity = responseFormattingExceptionHandler.handleDomainException(domainException, handlerMethod, locale)

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
  }
}
