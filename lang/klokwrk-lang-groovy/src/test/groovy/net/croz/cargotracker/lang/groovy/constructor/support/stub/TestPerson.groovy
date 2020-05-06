package net.croz.cargotracker.lang.groovy.constructor.support.stub

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable

@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class TestPerson implements PostMapConstructorCheckable {
  String firstName

  @SuppressWarnings("GroovyPointlessBoolean")
  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert firstName
    assert firstName.isBlank() == false
  }
}
