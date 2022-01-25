package org.klokwrk.cargotracker.booking.domain.model.value

import spock.lang.Specification

class CustomerSpecification extends Specification {
  void "map constructor should work for valid arguments"() {
    expect:
    new Customer(customerId: customerIdParam, customerType: customerTypeParam)

    where:
    customerIdParam                                         | customerTypeParam
    CustomerId.make("${ UUID.randomUUID() }")               | CustomerType.STANDARD
    CustomerId.make("00000000-0000-4000-8000-000000000000") | CustomerType.STANDARD
    CustomerId.make("00000000-0000-4000-9000-000000000001") | CustomerType.STANDARD
    CustomerId.make("11111111-1111-4111-A111-111111111111") | CustomerType.STANDARD

    CustomerId.make("${ UUID.randomUUID() }")               | CustomerType.ANONYMOUS
    CustomerId.make("${ UUID.randomUUID() }")               | CustomerType.GOLD
    CustomerId.make("${ UUID.randomUUID() }")               | CustomerType.PLATINUM
  }

  void "map constructor should fail for invalid arguments"() {
    when:
    new Customer(customerId: customerIdParam, customerType: customerTypeParam)

    then:
    AssertionError assertionError = thrown(AssertionError)
    assertionError.message.contains(errorMessagePartParam)

    where:
    customerIdParam                           | customerTypeParam     | errorMessagePartParam
    null                                      | CustomerType.STANDARD | "[item: customerId, expected: notNullValue(), actual: null]"
    CustomerId.make("${ UUID.randomUUID() }") | null                  | "[item: customerType, expected: notNullValue(), actual: null]"
  }
}
