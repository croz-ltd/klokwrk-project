/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
 * Data structure (a record) that defines specification for creating message codes suitable for resolving a message against resource bundle.
 */
@CompileStatic
class MessageSourceResolvableSpecification {
  /**
   * The uncapitalized simple name of a controller that is handling the current request.
   * <p/>
   * Typically, this is the uncapitalized name of the controller class name without the package. For example, if we have <code>org.example.MyController</code> controller class, controller's
   * uncapitalized simple name will be <code>myController</code>.
   * <p/>
   * The controller does not have to be HTTP/REST controller, but rather any controller type accepting requests from the client.
   */
  String controllerSimpleName

  /**
   * The method (or action) of the controller that is handling the current request.
   * <p/>
   * For example, if we have <code>org.example.MyController.myMethod()</code> controller method that is handling the current request, <code>controllerMethodName</code> will be <code>myMethod</code>.
   * <p/>
   * The controller does not have to be HTTP/REST controller, but rather any controller type accepting requests from the client.
   */
  String controllerMethodName

  /**
   * Category of a message.
   * <p/>
   * Usually, it describes the type of outcome of current request handling. For example, in case of errors, it is generally specified as <code>failure</code>. In case of validation failure, it might
   * be <code>validation</code> or <code>validationFailure</code>. If not specified, successful outcome might be implied. If there is a need to distinguish between various kinds of successful outcomes
   * it might contain some kind of success outcome categorisation.
   */
  String messageCategory

  /**
   * Inside of a message category, <code>messageType</code> determines more specific type of the outcome.
   * <p/>
   * For example, in HTTP/REST environment it might correspond to the string-message-encoded HTTP status. If we have status 500, <code>messageType</code> might be <code>internalServerError</code>.
   * For status 200, it might be just <code>ok</code>. It is not recommended to use numerical types since they are not easily recognisable when used as keys in resource bundle files.
   */
  String messageType

  /**
   * The sub-type of a message that is used when further categorisation of message types is needed.
   * <p/>
   * For example, say that we have some query controller that is trying to find and return <code>PersonSummary</code> entities based on some criteria. When controller cannot find the result,
   * corresponding <code>messageType</code> might be <code>notFound</code>. If this is not enough to get appropriate message, additional <code>messageSubType</code> might be defined with the value of
   * <code>personSummary</code>.
   */
  String messageSubType

  /**
   * Defines the severity of a message and should have <code>error</code>, <code>warning</code> or <code>info</code> values.
   */
  String severity

  /**
   * When message codes are created for some specific object and its properties, <code>propertyPath</code> might contain the path of relevant property.
   * <p/>
   * For example, if we are trying to resolve messages for an object that contains property <code>somePath</code>, and that <code>somePath</code> contains a nested property <code>message</code>,
   * the value of <code>propertyPath</code> should be <code>somePath.message</code>.
   */
  String propertyPath
}
