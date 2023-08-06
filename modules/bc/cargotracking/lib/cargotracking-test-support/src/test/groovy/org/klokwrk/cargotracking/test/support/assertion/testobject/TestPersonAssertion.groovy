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
package org.klokwrk.cargotracking.test.support.assertion.testobject

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.test.support.assertion.PageItemAssertionable

@CompileStatic
class TestPersonAssertion implements PageItemAssertionable {
  private final Map pageElementMap

  TestPersonAssertion(Map pageElementMap) {
    this.pageElementMap = pageElementMap
  }

  @Override
  TestPersonAssertion isSuccessful() {
    pageElementMap.with {
      assert size() == 3
      assert firstName
      assert lastName
      assert age instanceof Integer
      assert (age as Integer) >= 0
    }

    return this
  }

  TestPersonAssertion hasFirstName(String expectedFirstName) {
    assert pageElementMap.firstName == expectedFirstName
    return this
  }

  TestPersonAssertion hasLastName(String expectedLastName) {
    assert pageElementMap.lastName == expectedLastName
    return this
  }

  TestPersonAssertion hasAge(Integer expectedAge) {
    assert pageElementMap.age == expectedAge
    return this
  }
}
