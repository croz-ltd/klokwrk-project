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
package org.klokwrk.cargotracking.booking.lib.out.customer.port

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.value.Customer

/**
 * Outbound port for fetching {@link Customer} from external bounded context.
 */
@CompileStatic
interface CustomerByUserIdPortOut {
  /**
   * Finds {@link Customer} based on its user identifier.
   * <p/>
   * The user identifier can be many things. It can be something like a username or an email for registered users. For anonymous users, it can be an artificial identifier extracted from a cookie or
   * session id.
   * <p/>
   * It is vital to distinguish user identifiers from corresponding CustomerId. CustomerId is stored and remembered for registered users, and it is mapped to the current user identifier. While we can
   * change the user identifier and even delete it, we can not change the CustomerId. In customer management bounded context, CustomerId can be deleted, though. But events containing it cannot be
   * removed, of course.
   * <p/>
   * For anonymous users, CustomerId will probably be constructed on the fly and probably not stored in the customer management bounded context. The corresponding mapping to the user identifier will
   * be short-lived in that case. When an anonymous user deletes its temporary identifier (session closed or cookie deleted), there will be no way to access data related to the corresponding
   * CustomerId.
   * <p/>
   * The implementation of this operation may throw {@link org.klokwrk.cargotracking.lib.boundary.api.domain.exception.DomainException} if customer can not be found. Message key for such exception
   * can be something like '{@code customerByUserIdPortOut.findCustomerByUserId.notFound}'.
   */
  Customer findCustomerByUserId(String userId)
}
