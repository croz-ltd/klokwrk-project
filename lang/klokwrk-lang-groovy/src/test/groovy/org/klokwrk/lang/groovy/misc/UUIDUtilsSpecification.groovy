/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lang.groovy.misc

import spock.lang.Specification

class UUIDUtilsSpecification extends Specification {

  @SuppressWarnings("GroovyPointlessBoolean")
  void "checkIfRandomUuid - should return true for random UUID string"() {
    expect:
    UUIDUtils.checkIfRandomUuid(uuidStringParam) == true

    where:
    uuidStringParam                        | _
    "${ UUID.randomUUID() }"               | _
    "00000000-0000-4000-8000-000000000000" | _
    "00000000-0000-4000-9000-000000000000" | _
    "00000000-0000-4000-A000-000000000000" | _
    "00000000-0000-4000-B000-000000000000" | _
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "checkIfRandomUuid - should return false for non random UUID string"() {
    expect:
    UUIDUtils.checkIfRandomUuid(uuidStringParam) == false

    where:
    uuidStringParam                          | _
    null                                     | _
    ""                                       | _
    "   "                                    | _
    "123"                                    | _
    "00000000-0000-0000-0000-000000000000"   | _
    "00000000-0000-4000-0000-000000000000"   | _
    "00000000-0000-4000-1000-000000000000"   | _
    "00000000-0000-4000-7000-000000000000"   | _
    "00000000-0000-4000-C000-000000000000"   | _
    " 00000000-0000-4000-8000-000000000000"  | _
    "00000000-0000-4000-8000-000000000000 "  | _
    " 00000000-0000-4000-8000-000000000000 " | _
  }
}
