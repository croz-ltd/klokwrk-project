package org.klokwrk.cargotracker.booking.domain.model.service

import spock.lang.Specification

class ConstantBasedMaxAllowedTeuCountPolicySpecification extends Specification {
  void "constructor should fail for null parameter"() {
    when:
    new ConstantBasedMaxAllowedTeuCountPolicy(null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: maxAllowedTeuCount, expected: notNullValue(), actual: null]")
  }

  void "isTeuCountAllowed() method should work as expected"() {
    given:
    ConstantBasedMaxAllowedTeuCountPolicy constantBasedMaxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    Boolean isAllowed = constantBasedMaxAllowedTeuCountPolicy.isTeuCountAllowed(teuCountToCheckParam)

    then:
    isAllowed == isAllowedParam

    where:
    teuCountToCheckParam | isAllowedParam
    0.0                  | true
    1.0                  | true
    100.123              | true
    4999.9999            | true
    5000.0               | true
    5000.00001           | false
    5001.0               | false
  }
}
