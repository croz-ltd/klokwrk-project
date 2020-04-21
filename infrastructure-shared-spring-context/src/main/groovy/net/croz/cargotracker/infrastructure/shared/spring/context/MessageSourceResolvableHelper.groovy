package net.croz.cargotracker.infrastructure.shared.spring.context

import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.context.support.DefaultMessageSourceResolvable

@CompileStatic
class MessageSourceResolvableHelper {
  static String resolveMessageCodeList(MessageSource messageSource, List<String> messageCodeList, Locale locale) {
    String message = messageSource.getMessage(new DefaultMessageSourceResolvable(messageCodeList as String[]), locale)
    return message
  }

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
