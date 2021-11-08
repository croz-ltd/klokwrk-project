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
package org.klokwrk.lang.groovy.constructor.support

import org.klokwrk.lang.groovy.constructor.support.stub.TestPerson
import org.klokwrk.lang.groovy.constructor.support.stub.TestStrictPerson
import spock.lang.Specification

class PostMapConstructorCheckableSpecification extends Specification {
  void "should not throw for empty constructor by default"() {
    when:
    //noinspection GroovyImplicitNullArgumentCall
    new TestPerson()

    then:
    true
  }

  void "should throw for empty constructor when postMapConstructorShouldThrowForEmptyConstructorArguments() is overridden"() {
    when:
    //noinspection GroovyImplicitNullArgumentCall
    new TestStrictPerson()

    then:
    thrown(AssertionError)
  }

  void "should throw for invalid map constructor params"() {
    when:
    new TestPerson(firstName: firstNameParam)

    then:
    thrown(AssertionError)

    where:
    firstNameParam | _
    null           | _
    ""             | _
    "   "          | _
  }

  void "should post-process when checks are finished"() {
    when:
    TestPerson testPerson = new TestPerson(firstName: "SomeFirstName", lastName: "SomeLastName")

    then:
    testPerson.fullName == "${ testPerson.firstName } ${ testPerson.lastName }"
  }
}
