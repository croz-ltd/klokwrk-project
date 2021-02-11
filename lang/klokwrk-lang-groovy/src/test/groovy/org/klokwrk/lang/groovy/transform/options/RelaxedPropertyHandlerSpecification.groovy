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
package org.klokwrk.lang.groovy.transform.options

import spock.lang.Specification

class RelaxedPropertyHandlerSpecification extends Specification {
  void "should be strict with default MapConstructor"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor

        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    myClass.newInstance(myOtherInstance.properties)

    then:
    def exception = thrown(MissingPropertyException)
    exception.message == "No such property: address for class: MyClass"
  }

  void "should be relaxed with RelaxedPropertyHandler applied"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import groovy.transform.PropertyOptions
        import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

        @PropertyOptions(propertyHandler = RelaxedPropertyHandler)
        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    def myClassInstance = myClass.newInstance(myOtherInstance.properties)

    then:
    myClassInstance.first == "someFirst"
    myClassInstance.last == "someLast"
  }

  void "should work with additional AST transformations affected by PropertyOptions"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
        import groovy.transform.MapConstructor
        import groovy.transform.PropertyOptions
        import groovy.transform.Immutable
        import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

        @Immutable
        @PropertyOptions(propertyHandler = RelaxedPropertyHandler)
        @MapConstructor
        class MyClass {
          String first
          String last
        }
    """)

    Class myOtherClass = new GroovyClassLoader().parseClass("""
        class MyOtherClass {
          String first
          String last
          String address
        }
    """)

    def myOtherInstance = myOtherClass.newInstance(first: "someFirst", last: "someLast", address: "someAddress")

    when:
    def myClassInstance = myClass.newInstance(myOtherInstance.properties)

    then:
    myClassInstance.first == "someFirst"
    myClassInstance.last == "someLast"
  }
}
