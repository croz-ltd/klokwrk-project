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
package org.klokwrk.cargotracking.booking.lib.out.customer.adapter

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.booking.lib.out.customer.port.CustomerByUserIdPortOut
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo

import static org.hamcrest.Matchers.notNullValue
import static org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder.customer_anonymous
import static org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder.customer_gold
import static org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder.customer_platinum
import static org.klokwrk.cargotracking.domain.model.value.CustomerFixtureBuilder.customer_standard

/**
 * In-memory implementation of customer management bounded context.
 * <p/>
 * Of course, this implementation only contains what is needed in the booking context. All other aspects of customer management are ignored.
 */
@CompileStatic
class InMemoryCustomerRegistryService implements CustomerByUserIdPortOut {
  /**
   * Finds {@link Customer} based on its user identifier.
   * <p/>
   * If customer can not be found, implementation throws {@link DomainException}. Corresponding message key of the exception is
   * '{@code customerByUserIdPortOut.findCustomerByUserId.notFound}'.
   * <p/>
   * For concrete user identifiers and corresponding {@link org.klokwrk.cargotracking.domain.model.value.CustomerId}s and {@link CustomerType}s, please take a look at the source code.
   *
   * @see CustomerByUserIdPortOut#findCustomerByUserId(java.lang.String)
   */
  @Override
  Customer findCustomerByUserId(String userId) {
    requireMatch(userId, notNullValue())

    Customer customerFound = CustomerSample.findCustomerByUserId(userId)
    if (customerFound == null) {
      throw new DomainException(ViolationInfo.makeForBadRequestWithCustomCodeKey("customerByUserIdPortOut.findCustomerByUserId.notFound", [userId]))
    }

    return customerFound
  }

  static class CustomerSample {
    static final Map<String, Customer> CUSTOMER_SAMPLE_MAP = [
        "anonymous123": customer_anonymous().build(),
        "standard-customer@cargotracking.com": customer_standard().build(),
        "gold-customer@cargotracking.com": customer_gold().build(),
        "platinum-customer@cargotracking.com": customer_platinum().build()
    ]

    static Customer findCustomerByUserId(String userId) {
      Customer customerFound = CUSTOMER_SAMPLE_MAP.get(userId)
      return customerFound
    }
  }
}
