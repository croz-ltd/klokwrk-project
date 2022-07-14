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
package org.klokwrk.lang.groovy.transform

import spock.lang.Specification

class KwrkMapConstructorDefaultPostCheckAstTransformationSpecification extends Specification {
  void "should work with plain MapConstructor"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @MapConstructor
      @KwrkMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    myClass.newInstance(first: "Some first")

    then:
    AssertionError assertionError = thrown()
    assertionError.message.startsWith("assert last")
  }

  void "should work when additional single-arg non-map constructors are present"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @MapConstructor
      @KwrkMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        MyClass(String first) {
          this.first = first
        }

        MyClass(Integer someInt) {
        }

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    myClass.newInstance(first: "Some first")

    then:
    AssertionError assertionError = thrown()
    assertionError.message.startsWith("assert last")
  }

  void "should work when additional multi-arg non-map constructors are present"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @MapConstructor
      @KwrkMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        MyClass(String first, String last) {
          this.first = first
          this.last = last
        }

        MyClass(String first, String last, String something) {
          this.first = first
          this.last = last
        }

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    myClass.newInstance(first: "Some first")

    then:
    AssertionError assertionError = thrown()
    assertionError.message.startsWith("assert last")
  }

  void "should not be applied when not used from KwrkMapConstructorDefaultPostCheck annotation"() {
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.stub.KwrkFakeMapConstructorDefaultPostCheck

      @MapConstructor
      @KwrkFakeMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first")

    then:
    verifyAll(myClassInstance, {
      first == "Some first"
      last == null
    })
  }

  void "should not be applied when MapConstructor declares post argument"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @MapConstructor(post = {})
      @KwrkMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first")

    then:
    verifyAll(myClassInstance, {
      first == "Some first"
      last == null
    })
  }

  void "should not be applied when MapConstructor is not applied"() {
    given:
    Class myClass = new GroovyClassLoader().parseClass("""
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @KwrkMapConstructorDefaultPostCheck
      class MyClass implements PostMapConstructorCheckable {
        String first
        String last

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first")

    then:
    verifyAll(myClassInstance, {
      first == "Some first"
      last == null
    })
  }

  void "should not be applied when target class does not implement PostMapConstructorCheckable"() {
    Class myClass = new GroovyClassLoader().parseClass("""
      import groovy.transform.MapConstructor
      import org.klokwrk.lang.groovy.constructor.support.PostMapConstructorCheckable
      import org.klokwrk.lang.groovy.transform.KwrkMapConstructorDefaultPostCheck

      @MapConstructor
      @KwrkMapConstructorDefaultPostCheck
      class MyClass {
        String first
        String last

        void postMapConstructorCheck(Map<String, ?> constructorArguments) {
          assert first
          assert last
        }
      }
      """)

    when:
    def myClassInstance = myClass.newInstance(first: "Some first")

    then:
    verifyAll(myClassInstance, {
      first == "Some first"
      last == null
    })
  }
}
