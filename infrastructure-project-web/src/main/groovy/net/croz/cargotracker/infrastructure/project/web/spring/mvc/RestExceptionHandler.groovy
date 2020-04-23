package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableHelper
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableSpecification
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.response.ResponseReportViolationPart
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.DomainException
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.Severity
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationCode
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseReport
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseReportPart
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

import java.time.Instant

// useful references:
//   https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-exceptionhandler
//   https://www.baeldung.com/global-error-handler-in-a-spring-rest-api
//   https://blog.restcase.com/rest-api-error-handling-problem-details-response/
//   https://tools.ietf.org/html/rfc7807
@CompileStatic
class RestExceptionHandler extends ResponseEntityExceptionHandler implements MessageSourceAware {
  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleDomainException(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpResponseReport httpResponseReport = createHttpResponseReport(domainException, handlerMethod, locale)
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseReport.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseReport createHttpResponseReport(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)

    HttpResponseReport httpResponseReport = new HttpResponseReport(
        timestamp: Instant.now(),
        severity: Severity.WARNING,
        locale: locale,
        violation: createResponseReportViolationPart(domainException),
        http: createHttpResponseReportPart(httpStatus)
    )

    httpResponseReport = localizeHttpResponseReport(httpResponseReport, domainException, handlerMethod, locale)

    return httpResponseReport
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

  protected ResponseReportViolationPart createResponseReportViolationPart(DomainException domainException) {
    ResponseReportViolationPart responseReportViolationPart = new ResponseReportViolationPart(
        code: domainException.violationInfo.violationCode.code,
        codeMessage: domainException.violationInfo.violationCode.codeMessage
    )

    return responseReportViolationPart
  }

  protected HttpResponseReportPart createHttpResponseReportPart(HttpStatus httpStatus) {
    HttpResponseReportPart httpResponseReportPart = new HttpResponseReportPart(
        status: httpStatus.value().toString(),
        message: httpStatus.reasonPhrase
    )

    return httpResponseReportPart
  }

  protected HttpResponseReport localizeHttpResponseReport(HttpResponseReport httpResponseReport, DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.getBeanType().simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.getMethod().name,
        messageCategory: "failure",
        messageType: domainException.violationInfo.violationCode.codeAsText,
        messageSubType: "",
        severity: domainException.violationInfo.severity.toString().toLowerCase(),
        propertyPath: "report.titleText"
    )

    httpResponseReport.titleText = MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.titleDetailedText"
    httpResponseReport.titleDetailedText =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.violation.codeMessage"
    httpResponseReport.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseReport
  }
}
