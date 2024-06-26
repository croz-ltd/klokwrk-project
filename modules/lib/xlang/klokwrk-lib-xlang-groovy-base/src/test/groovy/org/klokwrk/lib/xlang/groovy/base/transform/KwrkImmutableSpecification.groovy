/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.xlang.groovy.base.transform

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.control.CompilationFailedException
import org.klokwrk.lib.xlang.groovy.base.transform.stub.Person
import spock.lang.Specification

import java.lang.reflect.Constructor

class KwrkImmutableSpecification extends Specification {
  void "should not statically compile for no-arg constructor since it is private"() {
    when:
    // NOTE: when used from dynamic code, Groovy will convert a call of new Person() into new Person(null) if explicit new Person() is not available. That later call, new Person(null) actually calls
    //       generated map constructor. For this reason, having private no-arg constructor does not have expected effect of compiler error when called from dynamic environment. We can only get
    //       compiler error from statically compiled environment.
    //       Reference: https://stackoverflow.com/a/31498804/4983601
    new GroovyShell().evaluate("""
        import groovy.transform.CompileStatic
        import org.klokwrk.lib.xlang.groovy.base.transform.stub.Person

        @CompileStatic
        class PersonFactory {
          static Person createWithNoArgConstructor() {
            return new Person()
          }
        }
    """)

    then:
    CompilationFailedException compilationFailedException = thrown()
    compilationFailedException.message.contains("Cannot find matching constructor org.klokwrk.lib.xlang.groovy.base.transform.stub.Person()")
  }

  void "should have private no-arg constructor"() {
    expect:
    Person.declaredConstructors.size() == 2
    Person.declaredConstructors.find({ Constructor constructor -> constructor.parameters.size() == 0 }).modifiers == Opcodes.ACC_PRIVATE
  }

  void "should work for empty map constructor"() {
    given:
    Person person = new Person([:])

    expect:
    person
    person.firstName == null
    person.lastName == null
  }

  void "should work for map constructor"() {
    given:
    Person person = new Person(firstName: "First Name", lastName: "Last Name")

    expect:
    person
    person.firstName == "First Name"
    person.lastName == "Last Name"
  }

  void "should throw for map constructor with invalid existing properties"() {
    when:
    new Person(firstName: "First Name")

    then:
    thrown(AssertionError)
  }

  void "should throw for map constructor with non-existing properties when existing properties are not used"() {
    when:
    //noinspection GroovyConstructorNamedArguments
    new Person(myAddress: "My Address")

    then:
    thrown(AssertionError)
  }

  void "should work for map constructor with non-existing properties"() {
    when:
    //noinspection GroovyConstructorNamedArguments
    Person person = new Person(firstName: "First Name", lastName: "Last Name", myAddress: "MyAddress")

    then:
    person
    person.firstName == "First Name"
    person.lastName == "Last Name"
    person.hasProperty("myAddress") == null
  }

  void "should generate expected toString() method"() {
    when:
    Person person = new Person(firstName: "First Name", lastName: "Last Name")

    then:
    person.toString() == "org.klokwrk.lib.xlang.groovy.base.transform.stub.Person(First Name, Last Name)"
  }

  void "should generate expected constructors"() {
    when:
    Class<Person> personClass = Person

    then:
    personClass.declaredConstructors.size() == 2
    personClass.declaredConstructors.find({ Constructor<?> constructor -> constructor.parameterTypes.find { Class<?> clazz -> clazz == Map } })
    personClass.declaredConstructors.find({ Constructor<?> constructor -> constructor.parameterTypes.size() == 0 })
  }

  void "should not generate tuple constructor"() {
    when:
    Class<Person> personClass = Person

    then:
    personClass.declaredConstructors.find({ Constructor<?> constructor -> constructor.parameterTypes.size() == 2 }) == null
  }
}
