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
import org.springframework.context.MessageSource
import org.springframework.context.support.DefaultMessageSourceResolvable

/**
 * Contains utility methods for creating message codes and resolving them via Spring's {@link MessageSource}.
 * <p/>
 * The most significant part are highly specialized static utility methods for creating message codes.
 * <p/>
 * At this moment, methods in this class and properties from {@code org.klokwrk.lib.spring.context.MessageSourceResolvableSpecification}, are highly specialized to localize parts of JSON responses
 * whose structure is defined by {@code ResponseFormatting*} interceptors from {@code org.klokwrk.cargotracker.lib.web.spring.mvc} package. Currently, only JSON responses representing failures have
 * parts that need to be localized.
 * <p/>
 * To better understand each utility method's purpose, it is useful to observe the complete message format of JSON responses representing failures. Here is an example for response roughly
 * corresponding to the failed validation:
 * <pre>
 * {
 *   "metaData": {
 *     "general": {
 *       "severity": "warning",
 *       "locale": "en_GB",
 *       "timestamp": "2020-04-26T09:41:04.917666Z"
 *     },
 *     "http": {  // Created only for JSON responses sent over HTTP channels
 *       "status": "400",
 *       "message": "Bad Request"
 *     },
 *     "violation": {
 *       "code": "400",
 *       "codeMessage": "Request is not valid.",
 *
 *       "logUuid": "123", // Created only for 'unknown' violations
 *
 *       "type": "validation|domain|other|unknown",
 *       }
 *     }
 *   },
 *   "payload": {} // In case of failures, payload is empty.
 * }
 * </pre>
 * There are several parts that need localization. For all failure types we have to localize {@code metaData.violation.codeMessage}. For validation failure types, localization is also needed for
 * {@code metaData.violation.validationReport.root.message} and {@code metaData.violation.validationReport.constraintViolations[].message}.
 * <p/>
 * Therefore, in case of validation failure, message codes for {@code metaData.violation.codeMessage} are created via {@code createMessageCodeListForViolationCodeMessageOfValidationFailure()} method.
 * Similarly, if we have a domain failure, we will use {@code createMessageCodeListForViolationCodeMessageOfDomainFailure()} method.
 * <p/>
 * Do note that the order of created message codes is significant and should go from the most specific message code first, then ending with more general elements. Spring's {@link MessageSource}
 * machinery will resolve messages trying codes in the given order, from first to last (the first match wins).
 * <p/>
 * In the lack of a better place, here we are presenting an example of the successful message. Its parts do not require any localization.
 * <pre>
 *   {
 *     "metaData": {
 *       "general": {
 *         "severity": "info",
 *         "locale": "en_GB",
 *         "timestamp": "2020-04-27T06:13:09.225Z",
 *       },
 *       "http": {
 *         "status": "200",
 *         "message": "OK"
 *       }
 *     },
 *     "payload": {
 *       ...
 *     }
 *   }
 * </pre>
 */
@CompileStatic
class MessageSourceResolvableHelper {
  /**
   * Resolves a message for given messageCodeList against Spring's {@link MessageSource}.
   */
  static String resolveMessageCodeList(MessageSource messageSource, List<String> messageCodeList, Locale locale, String defaultMessage = null) {
    String message = messageSource.getMessage(new DefaultMessageSourceResolvable(messageCodeList as String[], defaultMessage), locale)
    return message
  }

  /**
   * For {@code domain} category of failures, creates a list of message codes for resolving {@code metaData.violation.codeMessage} part of JSON response.
   * <p/>
   * Regarding {@link MessageSourceResolvableSpecification} properties, implementation fixes {@code messageCategory} to {@code failure} and {@code messageType} to {@code domain}. Significant and
   * distinguishing {@link MessageSourceResolvableSpecification} properties are {@code severity}, {@code messageSubType} and {@code messageSubTypeDetails}. This means that values of those properties
   * (beside {@code controllerSimpleName} and {@code controllerMethodName}) are used for creating message code permutations (ordered combinations).
   * <p/>
   * For domain failures, {@code severity} might be {@code error} or {@code warning}, where {@code warning} severity should be used in majority of cases. Domain warnings can indicate an invalid input
   * that clashes with business rules, although the request did pass semantic validation of input parameters. The clash can have various causes like inappropriate aggregate state or missing data in
   * related external registries. Since domain failures are always anticipated and controlled, error severity should be used rarely, if ever.
   * <p/>
   * The {@code messageSubType} categorizes domain failure, while {@code messageSubTypeDetails} provides more details if needed. The {@code messageSubTypeDetails} should be used only when
   * {@code messageSubType} is pretty general and not specific enough by itself.
   * <p/>
   * Say we have a request triggering command for accepting some kind of cargo. All request parameters are syntactically valid, but there is no route between provided locations, and consequently,
   * we cannot carry the cargo. In such a case, the command can raise a domain exception with {@code destinationLocationCannotAcceptCargo} code. When it comes to resolving of message codes for
   * {@code metaData.violation.codeMessage}, following codes will be generated.
   * <p/>
   * Example of message codes for {@code messageSubType = destinationLocationCannotAcceptCargo}.
   * <pre>
   *   "testController.testControllerMethod.failure.domain.warning.destinationLocationCannotAcceptCargo"
   *   "testController.testControllerMethod.failure.domain.destinationLocationCannotAcceptCargo"
   *   "testController.testControllerMethod.failure.domain.warning"
   *
   *   "testControllerMethod.failure.domain.warning.destinationLocationCannotAcceptCargo"
   *   "testControllerMethod.failure.domain.destinationLocationCannotAcceptCargo"
   *   "testControllerMethod.failure.domain.warning"
   *
   *   "default.failure.domain.warning.destinationLocationCannotAcceptCargo"
   *   "default.failure.domain.destinationLocationCannotAcceptCargo"
   *   "default.failure.domain.warning"
   *   "default.failure.domain"
   *   "default.failure.warning"
   *   "default.warning"
   * </pre>
   * In another example, we are issuing a query for the details about some cargo. Again, the given request contains all syntactically valid parameters, but our system cannot find the requested cargo.
   * Our query reacts by raising a domain exception containing {@code notFound} code. If we need to be more specific, the query can provide additional details about what exactly cannot be found. In
   * our case, this will be {@code cargoSummary}.
   * <p/>
   * Example of message codes for {@code messageSubType = notFound, messageSubTypeDetails = cargoSummary}:
   * <pre>
   *   "testController.testControllerMethod.failure.domain.warning.notFound.cargoSummary"
   *   "testController.testControllerMethod.failure.domain.warning.notFound"
   *   "testController.testControllerMethod.failure.domain.notFound.cargoSummary"
   *   "testController.testControllerMethod.failure.domain.notFound"
   *   "testController.testControllerMethod.failure.domain.warning"
   *
   *   "testControllerMethod.failure.domain.warning.notFound.cargoSummary"
   *   "testControllerMethod.failure.domain.warning.notFound"
   *   "testControllerMethod.failure.domain.notFound.cargoSummary"
   *   "testControllerMethod.failure.domain.notFound"
   *   "testControllerMethod.failure.domain.warning"
   *
   *   "default.failure.domain.warning.notFound.cargoSummary"
   *   "default.failure.domain.warning.notFound"
   *   "default.failure.domain.notFound.cargoSummary"
   *   "default.failure.domain.notFound"
   *   "default.failure.domain.warning"
   *   "default.failure.domain"
   *   "default.failure.warning"
   *   "default.warning"
   * </pre>
   */
  @SuppressWarnings("DuplicateStringLiteral")
  static List<String> createMessageCodeListForViolationCodeMessageOfDomainFailure(MessageSourceResolvableSpecification specification) {
    String controllerSimpleName = replaceWithDefaultIfEmpty(specification.controllerSimpleName)
    String controllerMethodName = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.controllerMethodName))
    String messageSubType = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.messageSubType))
    String messageSubTypeDetails = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.messageSubTypeDetails))
    String severity = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.severity, "warning"))

    String failureMessageCategory = ".failure"
    String domainMessageType = ".domain"

    List<String> messageCodeList = [
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ messageSubType }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }".toString(),

        "${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ messageSubType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ domainMessageType }${ severity }".toString(),

        "default${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "default${ failureMessageCategory }${ domainMessageType }${ severity }${ messageSubType }".toString(),
        "default${ failureMessageCategory }${ domainMessageType }${ messageSubType }${ messageSubTypeDetails }".toString(),
        "default${ failureMessageCategory }${ domainMessageType }${ messageSubType }".toString(),
        "default${ failureMessageCategory }${ domainMessageType }${ severity }".toString(),
        "default${ failureMessageCategory }${ domainMessageType }".toString(),
        "default${ failureMessageCategory }${ severity }".toString(),
        "default${ severity }".toString()
    ]

    messageCodeList = removeLeadingDot(messageCodeList)
    messageCodeList = removeStandaloneStrings(messageCodeList)

    return messageCodeList.unique()
  }

  /**
   * For {@code other} category of failures, creates a list of message codes for resolving {@code metaData.violation.codeMessage} part of JSON response.
   * <p/>
   * Regarding {@link MessageSourceResolvableSpecification} properties, implementation fixes {@code messageCategory} to {@code failure} and {@code messageType} to {@code other}. Significant and
   * distinguishing {@link MessageSourceResolvableSpecification} properties are {@code severity} and {@code messageSubType}. This means that values of those properties (beside
   * {@code controllerSimpleName} and {@code controllerMethodName}) are used for creating message code permutations (ordered combinations).
   * <p/>
   * Failure category {@code other} represents failures handled by some framework rather than by our infrastructure. In this category, we only have SpringMvc exceptions, but in the future additional
   * exceptions might end up here.
   * <p/>
   * Example of message codes for {@code severity = error, messageSubType = missingPathVariableException}.
   * <pre>
   *   "testController.testControllerMethod.failure.other.error.missingPathVariableException"
   *   "testController.testControllerMethod.failure.other.missingPathVariableException"
   *   "testController.testControllerMethod.failure.other.error"
   *
   *   "testControllerMethod.failure.other.error.missingPathVariableException"
   *   "testControllerMethod.failure.other.missingPathVariableException"
   *   "testControllerMethod.failure.other.error"
   *
   *   "default.failure.other.error.missingPathVariableException"
   *   "default.failure.other.missingPathVariableException"
   *   "default.failure.other.error"
   *   "default.failure.other"
   *   "default.failure.error"
   *   "default.error"
   * </pre>
   */
  @SuppressWarnings("DuplicateStringLiteral")
  static List<String> createMessageCodeListForViolationCodeMessageOfOtherFailure(MessageSourceResolvableSpecification specification) {
    String controllerSimpleName = replaceWithDefaultIfEmpty(specification.controllerSimpleName)
    String controllerMethodName = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.controllerMethodName))
    String messageSubType = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.messageSubType))
    String severity = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.severity, "warning"))

    String failureMessageCategory = ".failure"
    String otherMessageType = ".other"

    List<String> messageCodeList = [
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ severity }${ messageSubType }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ messageSubType }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ severity }".toString(),

        "${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ severity }${ messageSubType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ messageSubType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ otherMessageType }${ severity }".toString(),

        "default${ failureMessageCategory }${ otherMessageType }${ severity }${ messageSubType }".toString(),
        "default${ failureMessageCategory }${ otherMessageType }${ messageSubType }".toString(),
        "default${ failureMessageCategory }${ otherMessageType }${ severity }".toString(),
        "default${ failureMessageCategory }${ otherMessageType }".toString(),
        "default${ failureMessageCategory }${ severity }".toString(),
        "default${ severity }".toString()
    ]

    messageCodeList = removeLeadingDot(messageCodeList)
    messageCodeList = removeStandaloneStrings(messageCodeList)

    return messageCodeList.unique()
  }

  /**
   * For {@code unknown} category of failures, creates a list of message codes for resolving {@code metaData.violation.codeMessage} part of JSON response.
   * <p/>
   * Regarding {@link MessageSourceResolvableSpecification} properties, implementation fixes {@code messageCategory} to {@code failure}, {@code messageType} to {@code unknown}, and
   * {@code severity} to {@code error}. Significant and distinguishing {@link MessageSourceResolvableSpecification} property is {@code messageSubType}. This means that
   * the value of that property (beside {@code controllerSimpleName} and {@code controllerMethodName}) is used for creating message code permutations (ordered combinations).
   * <p/>
   * As unknown failures represent problems that are not anticipated, severity is always {@code error}. Therefore, implementation fixes severity value to {@code error}. Further,
   * {@code messageSubType} usually corresponds to the uncapitalized simple class name of the corresponding exception.
   * <p/>
   * Example of message codes for {@code messageSubType = runtimeException}.
   * <pre>
   *   "testController.testControllerMethod.failure.unknown.runtimeException"
   *   "testController.testControllerMethod.failure.unknown"
   *
   *   "testControllerMethod.failure.unknown.runtimeException"
   *   "testControllerMethod.failure.unknown"
   *
   *   "default.failure.unknown.runtimeException"
   *   "default.failure.unknown"
   *   "default.failure.error"
   *   "default.error"
   * </pre>
   */
  @SuppressWarnings("DuplicateStringLiteral")
  static List<String> createMessageCodeListForViolationCodeMessageOfUnknownFailure(MessageSourceResolvableSpecification specification) {
    String controllerSimpleName = replaceWithDefaultIfEmpty(specification.controllerSimpleName)
    String controllerMethodName = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.controllerMethodName))
    String messageSubType = prefixWithDotIfNotEmpty(replaceWithDefaultIfEmpty(specification.messageSubType))

    String failureMessageCategory = ".failure"
    String unknownMessageType = ".unknown"
    String errorSeverity = ".error"

    List<String> messageCodeList = [
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ unknownMessageType }${ messageSubType }".toString(),
        "${ controllerSimpleName }${ controllerMethodName }${ failureMessageCategory }${ unknownMessageType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ unknownMessageType }${ messageSubType }".toString(),
        "${ controllerMethodName }${ failureMessageCategory }${ unknownMessageType }".toString(),

        "default${ failureMessageCategory }${ unknownMessageType }${ messageSubType }".toString(),
        "default${ failureMessageCategory }${ unknownMessageType }".toString(),
        "default${ failureMessageCategory }${ errorSeverity }".toString(),
        "default${ errorSeverity }".toString()
    ]

    messageCodeList = removeLeadingDot(messageCodeList)
    messageCodeList = removeStandaloneStrings(messageCodeList)

    return messageCodeList.unique()
  }
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

  protected static String replaceWithDefaultIfEmpty(String stringParam, String defaultString = "") {
    String stringToReturn = stringParam?.trim() ?: defaultString
    return stringToReturn
  }

  protected static String prefixWithDotIfNotEmpty(String stringParam) {
    String stringToReturn = stringParam ? ".${ stringParam }" : stringParam
    return stringToReturn
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
