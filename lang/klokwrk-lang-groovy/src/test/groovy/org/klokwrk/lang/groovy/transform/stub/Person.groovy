package org.klokwrk.lang.groovy.transform.stub

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lang.groovy.transform.KwrkImmutable

@KwrkImmutable(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class Person implements PostMapConstructorCheckable {
  String firstName
  String lastName

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert firstName
    assert lastName
  }
}
