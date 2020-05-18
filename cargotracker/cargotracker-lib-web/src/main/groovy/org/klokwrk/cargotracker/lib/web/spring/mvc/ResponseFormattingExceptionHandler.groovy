package org.klokwrk.cargotracker.lib.web.spring.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.metadata.report.ResponseMetaDataReportViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationCode
import org.klokwrk.cargotracker.lib.web.conversation.metadata.HttpResponseMetaDataReport
import org.klokwrk.cargotracker.lib.web.conversation.metadata.HttpResponseMetaDataReportPart
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

  protected HttpResponseMetaDataReportPart createHttpResponseMetaDataReportPart(HttpStatus httpStatus) {
    HttpResponseMetaDataReportPart httpResponseMetaDataReportPart = new HttpResponseMetaDataReportPart(
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
        severity: domainException.violationInfo.severity.toString().toLowerCase(),
        propertyPath: "report.titleText"
    )

    httpResponseMetaDataReport.titleText =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.titleDetailedText"
    httpResponseMetaDataReport.titleDetailedText =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.violation.codeMessage"
    httpResponseMetaDataReport.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaDataReport
  }
}
