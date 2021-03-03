/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.spring.context

import groovy.transform.CompileStatic

/**
 * Data structure (a record) that defines the specification for creating message codes suitable for resolving messages against resource bundle.
 * <p/>
 * At this moment, properties in this class, and corresponding implementations of message code resolvers from {@code org.klokwrk.lib.spring.context.MessageSourceResolvableHelper}, are highly
 * specialized to localize parts of JSON responses whose structure is defined by {@code ResponseFormatting*} interceptors from {@code org.klokwrk.cargotracker.lib.web.spring.mvc} package.
 * <p/>
 * Be aware that actual implementations of message resolvers can choose to ignore any property from this specification.
 */
@CompileStatic
class MessageSourceResolvableSpecification {
  /**
   * The uncapitalized simple name of a controller that is handling the current request.
   * <p/>
   * Typically, this is the uncapitalized name of the controller class name without the package. For example, if we have {@code org.example.MyWebController} web controller class, controller's
   * uncapitalized simple name will be {@code myWebController}.
   * <p/>
   * The controller does not have to be an HTTP/REST controller, but rather any type of controller accepting the client's requests. For example, it can be a simple class name of the message handler
   * in messaging scenarios.
   */
  String controllerSimpleName

  /**
   * The method of the controller that is handling the current request.
   * <p/>
   * For example, if we have {@code org.example.MyWebController.myMethod()} web controller method that is handling the current HTTP request, {@code controllerMethodName} will be
   * {@code myMethod}.
   * <p/>
   * The controller does not have to be an HTTP/REST controller, but rather any type of controller accepting the client's requests. If used in messaging scenarios, {@code controllerMethodName} might
   * be a simple class name of handled message or event.
   */
  String controllerMethodName

  /**
   * High-level category of a message.
   * <p/>
   * Usually, it describes the type of outcome of current request handling. For our purposes, in case of errors, this property's value is specified as {@code failure}. If we need message
   * resolving for successful responses, this property's value should be {@code success}.
   */
  String messageCategory

  /**
   * Inside of a message category, this property determines a more specific type of outcome.
   * <p/>
   * For our purposes, in case of responses communicating some kind of error, {@code messageType} will contain a string which categorizes the failure type. For this purpose we are using lowercase
   * values of {@code org.klokwrk.cargotracker.lib.boundary.api.metadata.response.ViolationType} enum ({@code domain}, {@code validation}, etc.).
   */
  String messageType

  /**
   * The sub-type of a message that is used when further categorization of message types is needed.
   * <p/>
   * For example, in HTTP/REST environment it might correspond to the string-message-encoded HTTP status. If we have status 500, {@code messageType} will be {@code unknown} (indicating
   * unexpected/unknown error), while {@code messageSubType} might be {@code internalServerError}. It is not recommended to use numerical values here (i.e. 500) since they are harder to
   * comprehend when reading keys in resource bundle files.
   * <p/>
   * Let's look at another example. If we have some query controller trying to find and return entities based on some criteria. When the controller cannot find the result, the corresponding
   * {@code messageType} will be {@code domain} since we qualify that failure as a domain issue. But we still do not say anything more specific about the problem. This is where {@code messageSubType}
   * comes in, as it will contain {@code notFound} value.
   */
  String messageSubType

  /**
   * Details of message sub-type used for further and deeper categorization of message sub-types in {@code messageSubType}.
   * <p/>
   * If we need further categorization of {@code messageSubType}, we might use {@code messageSubTypeDetails}. For example, when localizing domain failure messages we might have {@code notFound}
   * {@code messageSubType} and {@code personSummary} {@code messageSubTypeDetails}. In resource bundle this will end up with {@code notFound.personSummary} sequence.
   */
  String messageSubTypeDetails

  /**
   * Specifies the severity of a message.
   * <p/>
   * The value of this property corresponds to the lowercase values of {@code org.klokwrk.cargotracker.lib.boundary.api.severity.Severity} enum ({@code error}, {@code warning}, and {@code info}).
   */
  String severity

}
