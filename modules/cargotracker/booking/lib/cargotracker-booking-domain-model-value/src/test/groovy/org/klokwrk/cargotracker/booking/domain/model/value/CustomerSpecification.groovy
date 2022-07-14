/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
