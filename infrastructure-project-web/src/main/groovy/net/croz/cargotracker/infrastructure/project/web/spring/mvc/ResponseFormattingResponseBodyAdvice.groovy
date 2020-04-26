package net.croz.cargotracker.infrastructure.project.web.spring.mvc

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableHelper
import net.croz.cargotracker.infrastructure.library.spring.context.MessageSourceResolvableSpecification
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationResponse
import net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation.Severity
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseReport
import net.croz.cargotracker.infrastructure.project.web.conversation.response.HttpResponseReportPart
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

    HttpResponseReport httpResponseReport = createHttpResponseReport(httpServletResponse, httpServletRequest)
    operationResponseBody.metaData = httpResponseReport.propertiesFiltered

    return operationResponseBody
  }

  protected HttpResponseReport createHttpResponseReport(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
    HttpStatus httpStatus = HttpStatus.resolve(httpServletResponse.status)

    HttpResponseReport httpResponseReport = new HttpResponseReport(
        timestamp: Instant.now(),
        severity: Severity.INFO,
        locale: httpServletRequest.getLocale(),
        http: createHttpResponseReportPart(httpStatus)
    )

    HandlerMethod handlerMethod = fetchHandlerMethod(httpServletRequest)
    if (handlerMethod) {
      httpResponseReport = localizeHttpResponseReport(httpResponseReport, handlerMethod, httpServletRequest.getLocale())
    }

    return httpResponseReport
  }

  protected HttpResponseReportPart createHttpResponseReportPart(HttpStatus httpStatus) {
    HttpResponseReportPart httpResponseReportPart = new HttpResponseReportPart(
        status: httpStatus.value().toString(),
        message: httpStatus.reasonPhrase
    )

    return httpResponseReportPart
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

  protected HttpResponseReport localizeHttpResponseReport(HttpResponseReport httpResponseReport, HandlerMethod handlerMethod, Locale locale) {
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
    httpResponseReport.titleText = MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    resolvableMessageSpecification.propertyPath = "report.titleDetailedText"
    httpResponseReport.titleDetailedText =
        MessageSourceResolvableHelper.resolveMessageCodeList(messageSource, MessageSourceResolvableHelper.createMessageCodeList(resolvableMessageSpecification), locale)

    return httpResponseReport
  }
}
