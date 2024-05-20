/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.domain.model.value.CustomerType

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CustomerEventDataFixtureBuilder {
  static CustomerEventDataFixtureBuilder customer_anonymous() {
    CustomerEventDataFixtureBuilder customerEventDataFixtureBuilder = new CustomerEventDataFixtureBuilder()
        .customerId("44f57e34-cfa5-4413-9329-4c2cc338c997")
        .customerType(CustomerType.ANONYMOUS)

    return customerEventDataFixtureBuilder
  }

  static CustomerEventDataFixtureBuilder customer_standard() {
    CustomerEventDataFixtureBuilder customerEventDataFixtureBuilder = new CustomerEventDataFixtureBuilder()
        .customerId("26d5f7d8-9ded-4ce3-b320-03a75f674f4e")
        .customerType(CustomerType.STANDARD)

    return customerEventDataFixtureBuilder
  }

  static CustomerEventDataFixtureBuilder customer_gold() {
    CustomerEventDataFixtureBuilder customerEventDataFixtureBuilder = new CustomerEventDataFixtureBuilder()
        .customerId("7517d07c-0031-4d4f-8d8f-58daeb3fad3c")
        .customerType(CustomerType.GOLD)

    return customerEventDataFixtureBuilder
  }

  static CustomerEventDataFixtureBuilder customer_platinum() {
    CustomerEventDataFixtureBuilder customerEventDataFixtureBuilder = new CustomerEventDataFixtureBuilder()
        .customerId("46af8019-f5fe-4b67-a514-e784b8bdde27")
        .customerType(CustomerType.PLATINUM)

    return customerEventDataFixtureBuilder
  }

  String customerId
  CustomerType customerType

  CustomerEventData build() {
    CustomerEventData customerEventData = new CustomerEventData(
        customerId: customerId, customerType: customerType
    )

    return customerEventData
  }
}
