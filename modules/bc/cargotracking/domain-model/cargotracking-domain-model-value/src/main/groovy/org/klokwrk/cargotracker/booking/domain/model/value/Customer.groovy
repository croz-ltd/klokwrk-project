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
package org.klokwrk.cargotracker.booking.domain.model.value

import groovy.transform.CompileStatic
import org.klokwrk.lib.xlang.groovy.base.constructor.support.PostMapConstructorCheckable
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import static org.hamcrest.Matchers.notNullValue

/**
 * Encapsulates attributes interesting in {@code booking} bounded context of the Customer aggregate from external bounded context responsible for Customer aggregate management.
 * <p/>
 * Encapsulated attributes are {@link CustomerId} and {@link CustomerType} and both must be {@code non-null} values. Further constraints are implemented inside those classes.
 * <p/>
 * In general, we will fetch customer representation for {@code booking} context through some kind of outbound adapter. Its implementation, or more probably, contacted external bounded context,
 * should be capable of mapping any type of <b>user identifier</b> to the real Customer aggregate in external bounded context. For example, a user identifier might be conventional as an email or
 * username or more advanced like a persistent cookie, bearer token, or session identifier.
 * <p/>
 * {@link CustomerId} encapsulated in this context's customer representation is none of those. Instead, {@code CustomerId} is an identifier for the Customer aggregate stored and managed by mentioned
 * external bounded context.
 * <p/>
 * Note: we don't have customer management context yet, but we can still simulate what some future (anti-corruption) outbound adapter should return in {@code booking} bounded context.
 */
@KwrkImmutable
@CompileStatic
class Customer implements PostMapConstructorCheckable {
  CustomerId customerId
  CustomerType customerType

  static Customer make(String customerIdString, CustomerType customerType) {
    return new Customer(customerId: CustomerId.make(customerIdString), customerType: customerType)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(customerId, notNullValue())
    requireMatch(customerType, notNullValue())
  }
}
