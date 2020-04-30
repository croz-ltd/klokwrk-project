package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableHelper
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableSpecification
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.ResponseMetaDataReportViolationPart
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.DomainException
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.ViolationCode
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseMetaDataReport
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseMetaDataReportPart
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
 * Produced HTTP response body is an instance of {@link OperationResponse} containing resolved "<code>metaData</code>" and empty "<code>payload</code>". When serialized into JSON it looks something
 * like following example:
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
 *     "titleText": "Warning",
 *     "timestamp": "2020-04-26T09:41:04.917666Z",
 *     "titleDetailedText": "Cargo is not booked since destination location cannot accept cargo from specified origin location."
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Here, internationalized "<code>metaData</code>" entries are "<code>violation.codeMessage</code>", "<code>titleText</code>" and "<code>titleDetailedText</code>".
 * <p/>
 * When used from Spring Boot application, the easiest is to create controller advice and register it with the spring context:
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingExceptionHandlerControllerAdvice extends ResponseFormattingExceptionHandler {
 * }
 *
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;Bean
 *   ResponseFormattingExceptionHandlerControllerAdvice responseFormattingExceptionHandlerControllerAdvice() {
 *     return new ResponseFormattingExceptionHandlerControllerAdvice()
 *   }
 * }
 * </pre>
 * For internationalization of default messages, we are defining a resource bundle with base name "<code>responseFormattingDefaultMessages</code>". In Spring Boot application, that resource bundle
 * needs to be configured, for example, in <code>application.yml</code>:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 * The list of message codes which will be tried against the resource bundle is created by {@link MessageSourceResolvableHelper}.
 *
 * @see MessageSourceResolvableHelper
 */
@CompileStatic
class ResponseFormattingExceptionHandler extends ResponseEntityExceptionHandler implements MessageSourceAware {
  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleDomainException(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpResponseMetaDataReport httpResponseReport = createHttpResponseReport(domainException, handlerMethod, locale)
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseReport.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), httpStatus)

    return responseEntity
  }

  protected HttpResponseMetaDataReport createHttpResponseReport(DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = mapDomainExceptionToHttpStatus(domainException)

    HttpResponseMetaDataReport httpResponseReport = new HttpResponseMetaDataReport(
        timestamp: Instant.now(),
        severity: domainException.violationInfo.severity,
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

  protected ResponseMetaDataReportViolationPart createResponseReportViolationPart(DomainException domainException) {
    ResponseMetaDataReportViolationPart responseReportViolationPart = new ResponseMetaDataReportViolationPart(
        code: domainException.violationInfo.violationCode.code,
        codeMessage: domainException.violationInfo.violationCode.codeMessage
    )

    return responseReportViolationPart
  }

  protected HttpResponseMetaDataReportPart createHttpResponseReportPart(HttpStatus httpStatus) {
    HttpResponseMetaDataReportPart httpResponseReportPart = new HttpResponseMetaDataReportPart(
        status: httpStatus.value().toString(),
        message: httpStatus.reasonPhrase
    )

    return httpResponseReportPart
  }

  protected HttpResponseMetaDataReport localizeHttpResponseReport(HttpResponseMetaDataReport httpResponseReport, DomainException domainException, HandlerMethod handlerMethod, Locale locale) {
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
