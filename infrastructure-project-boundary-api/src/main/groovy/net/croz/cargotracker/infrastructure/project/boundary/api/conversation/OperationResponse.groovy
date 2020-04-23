package net.croz.cargotracker.infrastructure.project.boundary.api.conversation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class OperationResponse<P> implements OperationMessage<P, Map<String, ?>>, PostMapConstructorCheckable {
  Map<String, ?> metaData = Collections.emptyMap()
  P payload

  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert metaData != null
    assert payload != null
  }
}
