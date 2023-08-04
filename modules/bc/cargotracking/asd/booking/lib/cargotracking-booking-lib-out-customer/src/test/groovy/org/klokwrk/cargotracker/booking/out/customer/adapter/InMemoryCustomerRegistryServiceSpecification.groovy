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
package org.klokwrk.cargotracker.booking.out.customer.adapter

import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.severity.Severity
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import spock.lang.Specification

class InMemoryCustomerRegistryServiceSpecification extends Specification {
  void "internal customer sample map should be of expected size"() {
    expect:
    InMemoryCustomerRegistryService.CustomerSample.CUSTOMER_SAMPLE_MAP.size() == 4
  }

  void "findCustomerByUserId() should find customer for existing user identifier"() {
    given:
    InMemoryCustomerRegistryService inMemoryCustomerRegistryService = new InMemoryCustomerRegistryService()

    when:
    Customer customer = inMemoryCustomerRegistryService.findCustomerByUserId(userIdParam)

    then:
    RandomUuidUtils.checkIfRandomUuidString(customer.customerId.identifier)
    customer.customerId.identifier == customerIdParam
    customer.customerType == customerTypeParam

    where:
    userIdParam                          | customerIdParam                        | customerTypeParam
    "anonymous123"                       | "44f57e34-cfa5-4413-9329-4c2cc338c997" | CustomerType.ANONYMOUS
    "standard-customer@cargotracker.com" | "26d5f7d8-9ded-4ce3-b320-03a75f674f4e" | CustomerType.STANDARD
    "gold-customer@cargotracker.com"     | "7517d07c-0031-4d4f-8d8f-58daeb3fad3c" | CustomerType.GOLD
    "platinum-customer@cargotracker.com" | "46af8019-f5fe-4b67-a514-e784b8bdde27" | CustomerType.PLATINUM
  }

  void "findCustomerByUserId() should fail for invalid param"() {
    given:
    InMemoryCustomerRegistryService inMemoryCustomerRegistryService = new InMemoryCustomerRegistryService()

    when:
    inMemoryCustomerRegistryService.findCustomerByUserId(null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("item: userId, expected: notNullValue(), actual: null")
  }

  void "findCustomerByUserId() should fail when Customer cannot be found"() {
    given:
    InMemoryCustomerRegistryService inMemoryCustomerRegistryService = new InMemoryCustomerRegistryService()

    when:
    inMemoryCustomerRegistryService.findCustomerByUserId("unknownUserId")

    then:
    DomainException domainException = thrown()
    domainException.violationInfo.severity == Severity.WARNING
    domainException.violationInfo.violationCode.code == "400"
    domainException.violationInfo.violationCode.codeMessage == "Bad Request"
    domainException.violationInfo.violationCode.resolvableMessageKey == "customerByUserIdPortOut.findCustomerByUserId.notFound"
    domainException.violationInfo.violationCode.resolvableMessageParameters == ["unknownUserId"]
  }
}
