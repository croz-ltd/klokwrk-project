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
package org.klokwrk.cargotracker.booking.out.customer.adapter

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.domain.model.value.CustomerType
import org.klokwrk.cargotracker.booking.out.customer.port.CustomerByUserIdentifierPortOut
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo

import static org.hamcrest.Matchers.notNullValue

/**
 * In-memory implementation of customer management bounded context.
 * <p/>
 * Of course, this implementation only contains what is needed in the booking context. All other aspects of customer management are ignored.
 */
@CompileStatic
class InMemoryCustomerRegistryService implements CustomerByUserIdentifierPortOut {
  /**
   * Finds {@link Customer} based on its user identifier.
   * <p/>
   * If customer can not be found, implementation throws {@link DomainException}. Corresponding message key of the exception is
   * '{@code customerByUserIdentifierPortOut.findCustomerByUserIdentifier.notFound}'.
   * <p/>
   * For concrete user identifiers and corresponding {@link org.klokwrk.cargotracker.booking.domain.model.value.CustomerId}s and {@link CustomerType}s, please take a look at the source code.
   *
   * @see CustomerByUserIdentifierPortOut#findCustomerByUserIdentifier(java.lang.String)
   */
  @Override
  Customer findCustomerByUserIdentifier(String userIdentifier) {
    requireMatch(userIdentifier, notNullValue())

    Customer customerFound = CustomerSample.findCustomerByUserIdentifier(userIdentifier)
    if (customerFound == null) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("customerByUserIdentifierPortOut.findCustomerByUserIdentifier.notFound", [userIdentifier]))
    }

    return customerFound
  }

  static class CustomerSample {
    static final Map<String, Customer> CUSTOMER_SAMPLE_MAP = [
        "anonymous123": Customer.make("44f57e34-cfa5-4413-9329-4c2cc338c997", CustomerType.ANONYMOUS),
        "standard-customer@cargotracker.com": Customer.make("26d5f7d8-9ded-4ce3-b320-03a75f674f4e", CustomerType.STANDARD),
        "gold-customer@cargotracker.com": Customer.make("7517d07c-0031-4d4f-8d8f-58daeb3fad3c", CustomerType.GOLD),
        "platinum-customer@cargotracker.com": Customer.make("46af8019-f5fe-4b67-a514-e784b8bdde27", CustomerType.PLATINUM)
    ]

    static Customer findCustomerByUserIdentifier(String userIdentifier) {
      Customer customerFound = CUSTOMER_SAMPLE_MAP.get(userIdentifier)
      return customerFound
    }
  }
}
