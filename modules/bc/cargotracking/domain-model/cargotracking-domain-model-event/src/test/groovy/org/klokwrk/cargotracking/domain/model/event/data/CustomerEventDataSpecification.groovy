/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.domain.model.event.data

import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder
import spock.lang.Specification

class CustomerEventDataSpecification extends Specification {
  void "fromCustomer() should work as expected"() {
    given:
    Customer customer = customerParam

    when:
    CustomerEventData customerEventData = CustomerEventData.fromCustomer(customer)

    then:
    customerEventData == customerEventDataExpectedParam

    where:
    customerParam                                       | customerEventDataExpectedParam
    CustomerFixtureBuilder.customer_anonymous().build() | CustomerEventDataFixtureBuilder.customer_anonymous().build()
    CustomerFixtureBuilder.customer_standard().build()  | CustomerEventDataFixtureBuilder.customer_standard().build()
    CustomerFixtureBuilder.customer_gold().build()      | CustomerEventDataFixtureBuilder.customer_gold().build()
    CustomerFixtureBuilder.customer_platinum().build()  | CustomerEventDataFixtureBuilder.customer_platinum().build()
  }

  void "toCustomer() should work as expected"() {
    given:
    CustomerEventData customerEventData = customerEventDataParam

    when:
    Customer customer = customerEventData.toCustomer()

    then:
    customer == customerExpectedParam

    where:
    customerEventDataParam                                       | customerExpectedParam
    CustomerEventDataFixtureBuilder.customer_anonymous().build() | CustomerFixtureBuilder.customer_anonymous().build()
    CustomerEventDataFixtureBuilder.customer_standard().build()  | CustomerFixtureBuilder.customer_standard().build()
    CustomerEventDataFixtureBuilder.customer_gold().build()      | CustomerFixtureBuilder.customer_gold().build()
    CustomerEventDataFixtureBuilder.customer_platinum().build()  | CustomerFixtureBuilder.customer_platinum().build()
  }
}
