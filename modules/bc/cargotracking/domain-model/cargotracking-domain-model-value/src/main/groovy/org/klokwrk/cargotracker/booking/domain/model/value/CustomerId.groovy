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
import org.klokwrk.lib.xlang.groovy.base.misc.RandomUuidUtils
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not

/**
 * Encapsulates an identifier of a Customer aggregate from external bounded context.
 * <p/>
 * Identifier does not have any business meaning. It is used just for referencing.
 * <p/>
 * Encapsulated identifier must exactly match whatever is used in external bounded context for Customer identifying.
 */
@KwrkImmutable
@CompileStatic
class CustomerId implements PostMapConstructorCheckable {
  String identifier

  /**
   * Factory method for creating {@code CustomerId} instance.
   *
   * @param uuidString String in random UUID format (UUID version 4, variant 2).
   */
  static CustomerId make(String uuidString) {
    return new CustomerId(identifier: uuidString)
  }

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    requireMatch(identifier, not(blankOrNullString()))
    requireMatch(RandomUuidUtils.checkIfRandomUuidString(identifier), is(true))
  }
}
