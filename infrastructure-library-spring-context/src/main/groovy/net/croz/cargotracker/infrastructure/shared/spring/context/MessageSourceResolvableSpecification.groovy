package net.croz.cargotracker.infrastructure.shared.spring.context

import groovy.transform.CompileStatic

@CompileStatic
class MessageSourceResolvableSpecification {
  String controllerSimpleName
  String controllerMethodName
  String messageCategory
  String messageType
  String messageSubType
  String severity
  String propertyPath
}
