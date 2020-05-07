package org.klokwrk.cargotracker.lib.boundary.api.conversation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

/**
 * Defines the basic format of response messages exchanged over domain facade boundary.
 *
 * @see OperationMessage
 */
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class OperationResponse<P> implements OperationMessage<P, Map<String, ?>>, PostMapConstructorCheckable {
  Map<String, ?> metaData = Collections.emptyMap()
  P payload

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert metaData != null
    assert payload != null
  }
}
