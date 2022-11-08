/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.cargotracker.lib.web.spring.mvc

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.application.metadata.response.ResponseMetaDataGeneralPart
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaData
import org.klokwrk.cargotracker.lib.web.metadata.response.HttpResponseMetaDataHttpPart
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.Instant

/**
 * Handles shaping and internationalization of the body in HTTP JSON responses when successful result of controller execution is {@link OperationResponse} instance.
 * <p/>
 * Produced HTTP response body is a modified instance of {@link OperationResponse} where modifications only affect "<code>metaData</code>", while "<code>payload</code>" is left unchanged.
 * "<code>metaData</code>" is affected by adding {@link HttpResponseMetaData} into it.
 * <p/>
 * When serialized into JSON it looks something like following example ("<code>payload</code>" is left out since it is not affected):
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "info",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-27T06:13:09.225221Z"
 *     },
 *     "http": {
 *       "status": "200",
 *       "message": "OK"
 *     }
 *   },
 *   "payload": {
 *     ...
 *   }
 * }
 * </pre>
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
    return returnType.parameterType == OperationResponse
  }

  @Override
  OperationResponse<?> beforeBodyWrite(
      OperationResponse<?> operationResponseBody, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse)
  {
    HttpServletRequest httpServletRequest = (serverHttpRequest as ServletServerHttpRequest).servletRequest
    HttpServletResponse httpServletResponse = (serverHttpResponse as ServletServerHttpResponse).servletResponse

    HttpResponseMetaData httpResponseMetaData = makeHttpResponseMetaData(httpServletResponse, httpServletRequest)
    operationResponseBody.metaData = httpResponseMetaData.getPropertiesFiltered() // codenarc-disable-line UnnecessaryGetter

    return operationResponseBody
  }

  protected HttpResponseMetaData makeHttpResponseMetaData(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
    HttpStatus httpStatus = HttpStatus.resolve(httpServletResponse.status)

    HttpResponseMetaData httpResponseMetaData = new HttpResponseMetaData(
        general: new ResponseMetaDataGeneralPart(timestamp: Instant.now(), severity: Severity.INFO.name().toLowerCase(), locale: httpServletRequest.locale),
        http: makeHttpResponseMetaDataPart(httpStatus)
    )

    return httpResponseMetaData
  }

  protected HttpResponseMetaDataHttpPart makeHttpResponseMetaDataPart(HttpStatus httpStatus) {
    HttpResponseMetaDataHttpPart httpResponseMetaDataPart = new HttpResponseMetaDataHttpPart(status: httpStatus.value().toString(), message: httpStatus.reasonPhrase)
    return httpResponseMetaDataPart
  }
}
