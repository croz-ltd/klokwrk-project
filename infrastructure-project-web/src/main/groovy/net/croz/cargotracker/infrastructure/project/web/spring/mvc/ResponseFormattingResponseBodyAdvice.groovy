package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableHelper
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableSpecification
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.Severity
import net.croz.cargotracker.infrastructure.project.web.conversation.metadata.HttpResponseMetaDataReport
import net.croz.cargotracker.infrastructure.project.web.conversation.metadata.HttpResponseMetaDataReportPart
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.MessageSource
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.Instant

/**
 * Handles shaping and internationalization of the body in HTTP JSON responses when successful result of controller execution is {@link OperationResponse} instance.
 * <p/>
 * Produced HTTP response body is a modified instance of {@link OperationResponse} where modifications only affect "<code>metaData</code>", while "<code>payload</code>" is left unchanged.
 * "<code>metaData</code>" is affected by adding {@link HttpResponseMetaDataReport} into it.
 * <p/>
 * When serialized into JSON it looks something like following example ("<code>payload</code>" is left out since it is not affected):
 * <pre>
 * {
 *   "metaData": {
 *     "http": {
 *       "status": "200",
 *       "message": "OK"
 *     },
 *     "severity": "INFO",
 *     "locale": "en_GB",
 *     "titleText": "Info",
 *     "timestamp": "2020-04-27T06:13:09.225Z",
 *     "titleDetailedText": "Your request is successfully executed."
 *   },
 *   "payload": {
 *     ...
 *   }
 * }
 * </pre>
 * Here, internationalized "<code>metaData</code>" entries are "<code>titleText</code>" and "<code>titleDetailedText</code>".
 * <p/>
 * When used from Spring Boot application, the easiest is to create controller advice and register it with the spring context:
 * <pre>
 * &#64;ControllerAdvice
 * class ResponseFormattingResponseBodyAdviceControllerAdvice extends ResponseFormattingResponseBodyAdvice {
 * }
 *
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;Bean
 *   ResponseFormattingResponseBodyAdviceControllerAdvice responseFormattingResponseBodyAdviceControllerAdvice() {
 *     return new ResponseFormattingResponseBodyAdviceControllerAdvice()
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
class ResponseFormattingResponseBodyAdvice implements ResponseBodyAdvice<OperationResponse<?>>, ApplicationContextAware {
  private ApplicationContext applicationContext

  @Override
  void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext
  }

  @Override
  boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return returnType.getParameterType() == OperationResponse
  }

  @Override
  OperationResponse<?> beforeBodyWrite(
      OperationResponse<?> operationResponseBody, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse)
  {
    HttpServletRequest httpServletRequest = (serverHttpRequest as ServletServerHttpRequest).servletRequest
    HttpServletResponse httpServletResponse = (serverHttpResponse as ServletServerHttpResponse).servletResponse

    HttpResponseMetaDataReport httpResponseMetaDataReport = createHttpResponseMetaDataReport(httpServletResponse, httpServletRequest)
    operationResponseBody.metaData = httpResponseMetaDataReport.propertiesFiltered

    return operationResponseBody
  }

  protected HttpResponseMetaDataReport createHttpResponseMetaDataReport(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
    HttpStatus httpStatus = HttpStatus.resolve(httpServletResponse.status)

    HttpResponseMetaDataReport httpResponseMetaDataReport = new HttpResponseMetaDataReport(
        timestamp: Instant.now(),
        severity: Severity.INFO,
        locale: httpServletRequest.getLocale(),
        http: createHttpResponseMetaDataReportPart(httpStatus)
    )

    HandlerMethod handlerMethod = fetchHandlerMethod(httpServletRequest)
    if (handlerMethod) {
      httpResponseMetaDataReport = localizeHttpResponseMetaDataReport(httpResponseMetaDataReport, handlerMethod, httpServletRequest.getLocale())
    }

    return httpResponseMetaDataReport
  }

  protected HttpResponseMetaDataReportPart createHttpResponseMetaDataReportPart(HttpStatus httpStatus) {
    HttpResponseMetaDataReportPart httpResponseMetaDataReportPart = new HttpResponseMetaDataReportPart(
        status: httpStatus.value().toString(),
        message: httpStatus.reasonPhrase
    )

    return httpResponseMetaDataReportPart
  }

  protected HandlerMethod fetchHandlerMethod(HttpServletRequest httpServletRequest) {
    Collection<HandlerMapping> handlerMappingCollection = applicationContext.getBeansOfType(HandlerMapping).values()
    HandlerMapping handlerMapping = handlerMappingCollection.find({ HandlerMapping handlerMapping -> handlerMapping.getHandler(httpServletRequest) })
    HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(httpServletRequest)
    Object handler = handlerExecutionChain.getHandler()

    if (handler !instanceof HandlerMethod) {
      return null
    }

    HandlerMethod handlerMethod = handler
    return handlerMethod
  }

  protected HttpResponseMetaDataReport localizeHttpResponseMetaDataReport(HttpResponseMetaDataReport httpResponseMetaDataReport, HandlerMethod handlerMethod, Locale locale) {
    MessageSourceResolvableSpecification resolvableMessageSpecification = new MessageSourceResolvableSpecification(
        controllerSimpleName: handlerMethod.getBeanType().simpleName.uncapitalize(),
        controllerMethodName: handlerMethod.getMethod().name,
        messageCategory: "success",
        messageType: "",
        messageSubType: "",
        severity: Severity.INFO.name().toLowerCase(),
        propertyPath: "report.titleText"
    )

    MessageSource messageSource = applicationContext
    httpResponseMetaDataReport.titleText = MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.titleDetailedText"
    httpResponseMetaDataReport.titleDetailedText =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseMetaDataReport
  }
}
