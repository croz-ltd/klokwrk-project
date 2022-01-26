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

import groovy.transform.CompileStatic

/**
 * Enumerates Customer types.
 * <p/>
 * Available Customer types are:
 * <ul>
 *   <li><b>ANONYMOUS</b>: not logged in customer. Does not have any benefits, i.e., like special booking discounts.</li>
 *   <li><b>STANDARD</b>: logged in customer. Usually does not have any benefits, i.e., like special booking discounts. It is equivalent to the anonymous, but logged in the system.</li>
 *   <li><b>GOLD</b>: logged in customer. Usually has some benefits like smaller discounts.</li>
 *   <li><b>PLATINUM</b>: logged in customer. It has benefits like larger discounts.</li>
 * </ul>
 */
@CompileStatic
enum CustomerType {
  ANONYMOUS,
  STANDARD,
  GOLD,
  PLATINUM
}
