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
   * Message code list is constructed from properties of provided {@link MessageSourceResolvableSpecification} parameter in (roughly), following way:
   * <pre>
   * List<String> messageCodeList = [
   *     "${ controllerSimpleName }${ controllerMethodName }${ messageCategory }${ messageType }${ messageSubType }${ severity }${ propertyPath }".toString(),
   *     "${ controllerSimpleName }${ controllerMethodName }${ messageCategory }${ messageType }${ messageSubType }${ propertyPath }".toString(),
   *
   *     "${ controllerSimpleName }${ controllerMethodName }${ messageCategory }${ severity }${ propertyPath }".toString(),
   *     "${ controllerSimpleName }${ controllerMethodName }${ messageCategory }${ propertyPath }".toString(),
   *
   *     "${ controllerMethodName }${ messageCategory }${ severity }${ propertyPath }".toString(),
   *     "${ controllerMethodName }${ messageCategory }${ propertyPath }".toString(),
   *
   *     "default${ messageCategory }${ messageType }${ messageSubType }${ severity }${ propertyPath }".toString(),
   *     "default${ messageCategory }${ messageType }${ messageSubType }${ propertyPath }".toString(),
   *
   *     "default${ messageCategory }${ severity }${ propertyPath }".toString(),
   *     "default${ messageCategory }${ propertyPath }".toString(),
   *     "default${ severity }${ propertyPath }".toString(),
   *     "default${ severity }".toString()
   * ]
   * <pre/>
   * For example, when a given {@link MessageSourceResolvableSpecification} instance contains following properties:
   * <pre>
   * MessageSourceResolvableSpecification {
   *   controllerSimpleName: "testController",
   *   controllerMethodName: "testControllerMethod",
   *   messageCategory: "failure",
   *   messageType: "internalServerError",
   *   messageSubType: "",
   *   severity: "error",
   *   propertyPath: "report.titleText"
   * }
   * </pre>
   * created message code list looks like this:
   * <pre>
   * [
   *   "testController.testControllerMethod.failure.internalServerError.error.report.titleText",
   *   "testController.testControllerMethod.failure.internalServerError.report.titleText",
   *   "testController.testControllerMethod.failure.error.report.titleText",
   *   "testController.testControllerMethod.failure.report.titleText",
   *   "testControllerMethod.failure.error.report.titleText",
   *   "testControllerMethod.failure.report.titleText",
   *   "testControllerMethod.failure.report.titleText",
   *   "default.failure.internalServerError.report.titleText",
   *   "default.failure.error.report.titleText",
   *   "default.failure.report.titleText",
   *   "default.error.report.titleText",
   *   "default.error"
   * ]
   * <pre>
   */
  @SuppressWarnings("CyclomaticComplexity")
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

        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerSimpleName }${ innerControllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),

        "${ controllerMethodName }${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "${ controllerMethodName }${ innerMessageCategory }${ innerPropertyPath }".toString(),

        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerMessageType }${ innerMessageSubType }${ innerPropertyPath }".toString(),

        "default${ innerMessageCategory }${ innerSeverity }${ innerPropertyPath }".toString(),
        "default${ innerMessageCategory }${ innerPropertyPath }".toString(),
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
