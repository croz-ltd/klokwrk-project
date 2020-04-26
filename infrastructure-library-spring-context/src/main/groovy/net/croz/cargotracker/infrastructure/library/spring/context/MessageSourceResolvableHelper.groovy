package net.croz.cargotracker.infrastructure.library.spring.context

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

  // @formatter:off
  /**
   * Creates a list of message codes that can be resolved via Spring's {@link MessageSource}.
   * <p/>
   * {@link MessageSource} tries to resolve a message starting from the first element of a given message code list (most specific message must be at the start of the list).
   * <p/>
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
  // @formatter:on
  static List<String> createMessageCodeList(MessageSourceResolvableSpecification specification) {
    String controllerSimpleName = specification.controllerSimpleName?.trim() ?: ""
    String controllerMethodName = specification.controllerMethodName?.trim() ?: ""
    String messageCategory = specification.messageCategory?.trim() ?: ""
    String messageType = specification.messageType?.trim() ?: ""
    String messageSubType = specification.messageSubType?.trim() ?: ""
    String severity = specification.severity?.trim() ?: "warning"
    String propertyPath = specification.propertyPath?.trim() ?: ""

    String innerControllerMethodName = controllerMethodName ? ".${controllerMethodName}" : controllerMethodName
    String innerMessageCategory = messageCategory ? ".${messageCategory}" : messageCategory
    String innerMessageType = messageType ? ".${messageType}" : messageType
    String innerMessageSubType = messageSubType ? ".${messageSubType}" : messageSubType
    String innerSeverity = ".${severity}"
    String innerPropertyPath = propertyPath ? ".${propertyPath}" : propertyPath

    List<String> messageCodeList = []

    messageCodeList << "${controllerSimpleName}${innerControllerMethodName}${innerMessageCategory}${innerMessageType}${innerMessageSubType}${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "${controllerSimpleName}${innerControllerMethodName}${innerMessageCategory}${innerMessageType}${innerMessageSubType}${innerPropertyPath}".toString()

    messageCodeList << "${controllerSimpleName}${innerControllerMethodName}${innerMessageCategory}${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "${controllerSimpleName}${innerControllerMethodName}${innerMessageCategory}${innerPropertyPath}".toString()

    messageCodeList << "${controllerMethodName}${innerMessageCategory}${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "${controllerMethodName}${innerMessageCategory}${innerPropertyPath}".toString()

    messageCodeList << "default${innerMessageCategory}${innerMessageType}${innerMessageSubType}${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "default${innerMessageCategory}${innerMessageType}${innerMessageSubType}${innerPropertyPath}".toString()

    messageCodeList << "default${innerMessageCategory}${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "default${innerMessageCategory}${innerPropertyPath}".toString()
    messageCodeList << "default${innerSeverity}${innerPropertyPath}".toString()
    messageCodeList << "default${innerSeverity}".toString()

    return messageCodeList.unique()
  }
}
