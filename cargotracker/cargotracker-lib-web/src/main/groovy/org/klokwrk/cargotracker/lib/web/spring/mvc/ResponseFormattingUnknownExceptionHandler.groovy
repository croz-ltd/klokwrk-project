package org.klokwrk.cargotracker.lib.web.spring.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ResponseMetaDataViolationPart
import org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.severity.Severity
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.klokwrk.lib.spring.context.MessageSourceResolvableHelper
import org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.HandlerMethod

import java.time.Instant

@CompileStatic
class ResponseFormattingUnknownExceptionHandler implements MessageSourceAware {
  static private final Logger log = LoggerFactory.getLogger(ResponseFormattingUnknownExceptionHandler)

  private MessageSource messageSource

  @Override
  void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource
  }

  @ExceptionHandler
  ResponseEntity handleUnknownException(Throwable unknownException, HandlerMethod handlerMethod, Locale locale) {
    String uuid = UUID.randomUUID().toString()
    log.error("Unknown exception occured [uuid: ${ uuid }, unknownExceptionClass: ${ unknownException.getClass().name }]", unknownException)

    HttpResponseMetaData httpResponseMetaData = createHttpResponseMetaData(unknownException, handlerMethod, locale)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)

    return responseEntity
  }

  protected HttpResponseMetaData createHttpResponseMetaData(Throwable unknownException, HandlerMethod handlerMethod, Locale locale) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    // TODO dmurat: add logUuid property
    ResponseMetaDataViolationPart responseMetaDataReportViolationPart =
        new ResponseMetaDataViolationPart(code: httpStatus.value().toString(), codeMessage: httpStatus.reasonPhrase, type: ViolationType.UNKNOWN)

    HttpResponseMetaDataHttpPart httpResponseMetaDataHttpPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.ERROR, locale: locale),
        violation: responseMetaDataReportViolationPart,
        http: httpResponseMetaDataHttpPart
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, handlerMethod, locale, unknownException)

    return httpResponseMetaData
  }

  protected HttpResponseMetaData localizeHttpResponseMetaData(HttpResponseMetaData httpResponseMetaDataReport, HandlerMethod handlerMethod, Locale locale, Throwable unknownException) {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: "unknown",
        messageSubType: unknownException.getClass().getSimpleName().uncapitalize(),
        severity: Severity.ERROR.toString().toLowerCase(),
        propertyPath: "httpResponseMetaData.violation.codeMessage"
    )

    httpResponseMetaDataReport.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaDataReport
  }
}
