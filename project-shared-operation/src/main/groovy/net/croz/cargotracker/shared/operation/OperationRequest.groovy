package net.croz.cargotracker.shared.operation

import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
class OperationRequest<P> implements OperationMessage<P, Map<String, ?>>, PostMapConstructorCheckable {
  P payload
  Map<String, ?> metaData = Collections.emptyMap()

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert metaData != null
    assert payload != null
  }
}
