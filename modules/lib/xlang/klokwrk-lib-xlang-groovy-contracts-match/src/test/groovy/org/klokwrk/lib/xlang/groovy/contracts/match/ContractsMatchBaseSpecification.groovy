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
package org.klokwrk.lib.xlang.groovy.contracts.match

import spock.lang.Specification

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.emptyString
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not

class ContractsMatchBaseSpecification extends Specification {
  static class Person {
    String firstName
    String lastName
  }

  void "should throw for invalid matcher parameter"() {
    when:
    ContractsMatchBase.requireMatchBase("123", null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[condition: (matcher != null)]")
  }

  void "should throw for mismatch"() {
    when:
    ContractsMatchBase.requireMatchBase("123", is(emptyString()))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: 123, expected: is an empty string, actual: 123]")
  }

  void "should throw for mismatch with custom messages"() {
    when:
    ContractsMatchBase.requireMatchBase("123", is(emptyString()), itemDescritpion, matcherDescription)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains(messageContainedString)

    where:
    itemDescritpion    | matcherDescription    | messageContainedString
    null               | null                  | "[item: 123, expected: is an empty string, actual: 123]"
    ""                 | null                  | "[item: 123, expected: is an empty string, actual: 123]"
    null               | ""                    | "[item: 123, expected: is an empty string, actual: 123]"
    ""                 | ""                    | "[item: 123, expected: is an empty string, actual: 123]"
    "item description" | ""                    | "[item: item description, expected: is an empty string, actual: 123]"
    ""                 | "matcher description" | "[item: 123, expected: matcher description, actual: 123]"
    "item description" | "matcher description" | "[item: item description, expected: matcher description, actual: 123]"
  }

  void "should not throw when matching"() {
    when:
    ContractsMatchBase.requireMatchBase("123", not(emptyString()))

    then:
    true
  }

  void "should throw for invalid object's properties with custom messages"() {
    given:
    Person person = new Person(firstName: "First Name")

    when:
    ContractsMatchBase.requireMatchBase(person.lastName, not(blankOrNullString()), "person.lastName", "not(blankOrNullString())")

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: person.lastName, expected: not(blankOrNullString()), actual: null]")
  }
}
