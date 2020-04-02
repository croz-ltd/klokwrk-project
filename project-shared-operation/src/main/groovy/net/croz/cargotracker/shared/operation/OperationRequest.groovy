package net.croz.cargotracker.shared.operation

import net.croz.cargotracker.lang.groovy.transform.MapConstructorRelaxed

@MapConstructorRelaxed(post = { postConstructCheck() })
class OperationRequest<P> implements OperationMessage<P, Map<String, ?>> {
  P payload
  Map<String, ?> metaData = Collections.emptyMap()

  void postConstructCheck() {
    assert metaData != null
    assert payload != null
  }
}
