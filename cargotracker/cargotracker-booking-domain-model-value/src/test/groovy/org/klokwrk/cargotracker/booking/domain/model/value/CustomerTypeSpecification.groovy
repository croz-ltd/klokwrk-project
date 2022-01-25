package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class CustomerTypeSpecification extends Specification {
  void "should have expected enum size"() {
    // Failure of this test is a signal that we should check places where enumeration is used and update tests and switch/if/else statements
    expect:
    CustomerType.values().size() == 4
  }
}
