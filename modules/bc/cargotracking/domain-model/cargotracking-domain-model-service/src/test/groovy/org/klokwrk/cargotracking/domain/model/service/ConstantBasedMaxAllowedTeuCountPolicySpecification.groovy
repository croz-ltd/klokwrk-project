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
package org.klokwrk.cargotracking.domain.model.service

import spock.lang.Specification

class ConstantBasedMaxAllowedTeuCountPolicySpecification extends Specification {
  void "constructor should fail for null parameter"() {
    when:
    new ConstantBasedMaxAllowedTeuCountPolicy(null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: maxAllowedTeuCount, expected: notNullValue(), actual: null]")
  }

  void "isTeuCountAllowed() method should work as expected"() {
    given:
    ConstantBasedMaxAllowedTeuCountPolicy constantBasedMaxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    when:
    boolean isAllowed = constantBasedMaxAllowedTeuCountPolicy.isTeuCountAllowed(teuCountToCheckParam)

    then:
    isAllowed == isAllowedParam

    where:
    teuCountToCheckParam | isAllowedParam
    0.0                  | true
    1.0                  | true
    100.123              | true
    4999.9999            | true
    5000.0               | true
    5000.00001           | false
    5001.0               | false
  }
}
