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
import org.springframework.context.MessageSource
import org.springframework.context.support.DefaultMessageSourceResolvable

/**
 * Contains static utility methods for creating and resolving message codes via Spring's {@link MessageSource}.
 */
@CompileStatic
class MessageSourceResolvableHelper {
  /**
   * Resolves a message for given messageCodeList against Spring's {@link MessageSource}.
   */
  static String resolveMessageCodeList(MessageSource messageSource, List<String> messageCodeList, Locale locale) {
    String message = messageSource.getMessage(new DefaultMessageSourceResolvable(messageCodeList as String[]), locale)
    return message
  }

  /**
   * Creates a list of message codes that can be resolved via Spring's {@link MessageSource}.
   * <p/>
   * The order of list elements is significant, and in general should go from most specific message code first, then ending with more general elements. Spring's {@link MessageSource} machinery will
   * try to resolve message codes in given order, from first to last.
   * <p/>
   * Message code list is constructed from properties of provided {@link MessageSourceResolvableSpecification} parameter in the following way:
   * <pre>
   * List<String> messageCodeList = [
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }".toString(),
   *
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerSeverity }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }".toString(),
   *
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerSeverity }".toString(),
   *   "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }".toString(),
   *
   *   "${ controllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "${ controllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),
   *   "${ controllerMethodName }${ innerMessageCategory }${ innerSeverity }".toString(),
   *   "${ controllerMethodName }${ innerMessageCategory }".toString(),
   *
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }".toString(),
   *
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }${ innerSeverity }".toString(),
   *   "default${ innerMessageCategory }${ innerMessageType }".toString(),
   *
   *   "default${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerPropertyPath }".toString(),
   *   "default${ innerMessageCategory }${ innerSeverity }".toString(),
   *   "default${ innerMessageCategory }".toString(),
   *
   *   "default${ innerSeverity }${ innerPropertyPath }".toString(),
   *   "default${ innerSeverity }".toString()
   * ]
   * </pre>
   *
   * For example, in case of error we might use {@link MessageSourceResolvableSpecification} instance with the following properties:<br/><br/>
   *
   * <pre>
   * new MessageSourceResolvableSpecification(
   *   controllerSimpleName: "testController",
   *   controllerMethodName: "testControllerMethod",
   *   messageCategory: "failure",
   *   messageType: "unknown",
   *   messageSubType: "internalServerError",
   *   severity: "error",
   *   propertyPath: "somePath.message"
   * )
   * </pre>
   *
   * For the failure specification above, created message code list looks like this:<br/><br/>
   *
   * <pre>
   * [
   *    "testController.testControllerMethod.failure.unknown.internalServerError.error.somePath.message",
   *    "testController.testControllerMethod.failure.unknown.internalServerError.somePath.message",
   *    "testController.testControllerMethod.failure.unknown.internalServerError.error",
   *    "testController.testControllerMethod.failure.unknown.internalServerError",
   *
   *    "testController.testControllerMethod.failure.unknown.error.somePath.message",
   *    "testController.testControllerMethod.failure.unknown.somePath.message",
   *    "testController.testControllerMethod.failure.unknown.error",
   *    "testController.testControllerMethod.failure.unknown",
   *
   *    "testController.testControllerMethod.failure.error.somePath.message",
   *    "testController.testControllerMethod.failure.somePath.message",
   *    "testController.testControllerMethod.failure",
   *    "testController.testControllerMethod.failure.error",
   *
   *    "testControllerMethod.failure.error.somePath.message",
   *    "testControllerMethod.failure.somePath.message",
   *    "testControllerMethod.failure.error",
   *    "testControllerMethod.failure",
   *
   *    "default.failure.unknown.internalServerError.error.somePath.message",
   *    "default.failure.unknown.internalServerError.somePath.message",
   *    "default.failure.unknown.internalServerError.error",
   *    "default.failure.unknown.internalServerError",
   *
   *    "default.failure.unknown.error.somePath.message",
   *    "default.failure.unknown.somePath.message",
   *    "default.failure.unknown.error",
   *    "default.failure.unknown",
   *
   *    "default.failure.error.somePath.message",
   *    "default.failure.somePath.message",
   *    "default.failure.error",
   *    "default.failure",
   *
   *    "default.error.somePath.message",
   *    "default.error"
   * ]
   * </pre>
   *
   * In case of success, {@link MessageSourceResolvableSpecification} instance might look like the following example:<br/><br/>
   * <pre>
   * new MessageSourceResolvableSpecification(
   *   controllerSimpleName: "testController",
   *   controllerMethodName: "testControllerMethod",
   *   messageCategory: "success",
   *   messageType: "",
   *   messageSubType: "",
   *   severity: "info",
   *   propertyPath: "somePath.message"
   * )
   * </pre>
   *
   * Corresponding message code list is:<br/><br/>
   * <pre>
   * [
   *   "testController.testControllerMethod.success.info.somePath.message",
   *   "testController.testControllerMethod.success.somePath.message",
   *   "testController.testControllerMethod.success.info",
   *   "testController.testControllerMethod.success",
   *   "testControllerMethod.success.info.somePath.message",
   *   "testControllerMethod.success.somePath.message",
   *   "testControllerMethod.success.info",
   *   "testControllerMethod.success",
   *   "default.success.info.somePath,.message",
   *   "default.success.somePath.message",
   *   "default.success.info",
   *   "default.success",
   *   "default.info.somePath.message",
   *   "default.info"
   * ]
   * </pre>
   */
  @SuppressWarnings(["CyclomaticComplexity", "AbcMetric"])
  static List<String> createMessageCodeList(MessageSourceResolvableSpecification specification) {
    String controllerSimpleName = specification.controllerSimpleName?.trim() ?: ""
    String controllerMethodName = specification.controllerMethodName?.trim() ?: ""
    String messageCategory = specification.messageCategory?.trim() ?: ""
    String messageType = specification.messageType?.trim() ?: ""
    String messageSubType = specification.messageSubType?.trim() ?: ""
    String severity = specification.severity?.trim() ?: "warning"
    String propertyPath = specification.propertyPath?.trim() ?: ""

    String innerControllerMethodName = controllerMethodName ? ".${ controllerMethodName }" : controllerMethodName
    String innerMessageCategory = messageCategory ? ".${ messageCategory }" : messageCategory
    String innerMessageType = messageType ? ".${ messageType }" : messageType
    String innerMessageSubType = messageSubType ? ".${ messageSubType }" : messageSubType
    String innerSeverity = ".${ severity }"
    String innerPropertyPath = propertyPath ? ".${ propertyPath }" : propertyPath

    List<String> messageCodeList = [
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }".toString(),

        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }${ innerSeverity }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerMessageType }".toString(),

        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerSeverity }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }".toString(),

        "${ controllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),
        "${ controllerMethodName }${ innerMessageCategory }${ innerSeverity }".toString(),
        "${ controllerMethodName }${ innerMessageCategory }".toString(),

        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }".toString(),

        "default${ innerMessageCategory }${ innerMessageType }${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerSeverity }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }".toString(),

        "default${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerSeverity }".toString(),
        "default${ innerMessageCategory }".toString(),

        "default${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerSeverity }".toString()
    ]

    messageCodeList = removeLeadingDot(messageCodeList)
    messageCodeList = removeStandaloneStrings(messageCodeList)

    return messageCodeList.unique()
  }

  protected static List<String> removeLeadingDot(List<String> messageCodeList) {
    List<String> withoutLeadingDotMessageCodeList = messageCodeList.collect { String messageCode ->
      if (!messageCode) {
        return messageCode
      }

      if (messageCode[0] == ".") {
        return messageCode[1..-1]
      }

      return messageCode
    }

    return withoutLeadingDotMessageCodeList
  }

  /**
   * Filters out any standalone (without containing dots) strings, i.e. empty strings, "warning", "default" etc.
   */
  protected static List<String> removeStandaloneStrings(List<String> messageCodeList) {
    List<String> withoutStandaloneStringsMessageCodeList = messageCodeList.findAll { String messageCode ->
      messageCode.contains(".")
    }

    return withoutStandaloneStringsMessageCodeList
  }
}
