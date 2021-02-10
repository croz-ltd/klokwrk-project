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

/**
 * Handles shaping and internationalization of the body in HTTP responses when execution of controller results in throwing unpredicted exception.
 * <p/>
 * Produced HTTP response body is a JSON serialized from {@link OperationResponse} instance containing populated "<code>metaData</code>" and empty "<code>payload</code>" properties. Here is an
 * example:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "ERROR",
 *       "locale": "en_GB",
 *       "timestamp": "2021-02-09T10:09:36.354151Z"
 *     },
 *     "http": {
 *       "status": "500",
 *       "message": "Internal Server Error"
 *     },
 *     "violation": {
 *       "code": "500",
 *       "codeMessage": "Internal server error.",
 *       "type": "UNKNOWN",
 *       "logUuid": "116be9a6-9f38-4954-8b8f-e57e781655d0"
 *     }
 *   },
 *   "payload": {}
 * }
 * </pre>
 * Here, "<code>violation.codeMessage</code>" entry is internationalized.
 * <p/>
 * When used from Spring Boot application, the easiest is to create a controller advice that is eligible for component scanning (&#64;ControllerAdvice is annotated with &#64;Component):
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingUnknownExceptionHandlerControllerAdvice extends ResponseFormattingUnknownExceptionHandler {
 * }
 * </pre>
 * For internationalization of default messages, we are defining a resource bundle with base name "<code>responseFormattingDefaultMessages</code>". In Spring Boot application, that resource bundle
 * needs to be configured, for example, in <code>application.yml</code>:
 * <pre>
 * ...
 * spring.messages.basename: messages,responseFormattingDefaultMessages
 * ...
 * </pre>
 * The list of message codes which will be tried against the resource bundle is created by {@link MessageSourceResolvableHelper}. For resolving messages we are using
 * <code>httpResponseMetaData.violation.codeMessage</code> for <code>propertyPath</code> property of <code>MessageSourceResolvableSpecification</code>. This is to avoid potential future conflicts in
 * resource bundle keys if we'll need message resolving over some other <code>propertyPath</code>.
 * <p/>
 * Here is a list of <code>MessageSourceResolvableSpecification</code> property values used for resolving internationalized messages:
 * <ul>
 *   <li>controllerSimpleName: simple class name (without package) of a controller that was executing when an exception occurred</li>
 *   <li>controllerMethodName: method name of a controller that was executing when an exception occurred</li>
 *   <li>messageCategory: <code>failure</code></li>
 *   <li>messageType: <code>unknown</code></li>
 *   <li>messageSubType: simple class name (without package) of exception</li>
 *   <li>severity: <code>error</code></li>
 *   <li>propertyPath: <code>httpResponseMetaData.violation.codeMessage</code></li>
 * </ul>
 *
 * @see MessageSourceResolvableHelper
 */
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
    String logUuid = UUID.randomUUID()
    log.error("Unknown exception occured [uuid: ${ logUuid }, unknownExceptionClass: ${ unknownException.getClass().name }]", unknownException)

    HttpResponseMetaData httpResponseMetaData = createHttpResponseMetaData(unknownException, handlerMethod, locale, logUuid)
    OperationResponse operationResponse = new OperationResponse(payload: [:], metaData: httpResponseMetaData.propertiesFiltered)
    ResponseEntity responseEntity = new ResponseEntity(operationResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)

    return responseEntity
  }

  protected HttpResponseMetaData createHttpResponseMetaData(Throwable unknownException, HandlerMethod handlerMethod, Locale locale, String logUuid) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    ResponseMetaDataViolationPart responseMetaDataReportViolationPart =
        new ResponseMetaDataViolationPart(code: httpStatus.value().toString(), codeMessage: httpStatus.reasonPhrase, type: ViolationType.UNKNOWN, logUuid: logUuid)

    HttpResponseMetaDataHttpPart httpResponseMetaDataHttpPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.ERROR, locale: locale),
        violation: responseMetaDataReportViolationPart,
        http: httpResponseMetaDataHttpPart
    )

    httpResponseMetaData = localizeHttpResponseMetaData(httpResponseMetaData, handlerMethod, locale, unknownException)

    return httpResponseMetaData
  }

  protected HttpResponseMetaData localizeHttpResponseMetaData(HttpResponseMetaData httpResponseMetaData, HandlerMethod handlerMethod, Locale locale, Throwable unknownException) {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.beanType.simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.method.name,
        messageCategory: "failure",
        messageType: "unknown",
        messageSubType: unknownException.getClass().simpleName.uncapitalize(),
        severity: Severity.ERROR.toString().toLowerCase(),
        propertyPath: "httpResponseMetaData.violation.codeMessage"
    )

    httpResponseMetaData.violation.codeMessage =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaData
  }
}
