package org.klokwrk.cargotracker.lib.boundary.api.operation

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require

/**
 * Defines the basic format of request messages exchanged over domain facade boundary.
 *
 * @see OperationMessage
 */
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class OperationRequest<P> implements OperationMessage<P, Map<String, ?>>, PostMapConstructorCheckable {
  Map<String, ?> metaData = Collections.emptyMap()
  P payload

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    require(metaData, notNullValue())
    require(payload, notNullValue())
  }
}
