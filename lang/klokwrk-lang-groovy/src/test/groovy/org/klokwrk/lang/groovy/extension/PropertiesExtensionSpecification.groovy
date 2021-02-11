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
package org.klokwrk.lang.groovy.extension

import spock.lang.Specification

class PropertiesExtensionSpecification extends Specification {
  class MyPogoPerson {
    String firstName
    String lastName
  }

  @SuppressWarnings("GroovyPointlessBoolean")
  void "getPropertiesFiltered - should return a map without properties excluded by default"() {
    given:
    MyPogoPerson person = new MyPogoPerson(firstName: "myFirstName", lastName: "myLastName")

    when:
    Map<String, ?> filteredProperties = person.propertiesFiltered

    then:
    filteredProperties.findAll({ it.key !in PropertiesExtension.DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST }).size() == 2
    filteredProperties.findAll({ it.key !in PropertiesExtension.DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST }).collect({ it.key }).containsAll(["firstName", "lastName"])

    filteredProperties.any({ it.key in PropertiesExtension.DEFAULT_FILTER_OUT_PROPERTY_NAME_LIST }) == false
  }

  void "getPropertiesFiltered(Map) - should return a map without properties excluded by parameter"() {
    given:
    MyPogoPerson person = new MyPogoPerson(firstName: "myFirstName", lastName: "myLastName")

    when:
    Map<String, ?> filteredProperties = person.getPropertiesFiltered(["lastName"])

    then:
    filteredProperties.size() == 2
    filteredProperties.collect({ it.key }).containsAll(["firstName", "class"])
  }

  void "getPropertiesFiltered(Map) - should throw for invalid parameter"() {
    given:
    MyPogoPerson person = new MyPogoPerson(firstName: "myFirstName", lastName: "myLastName")

    when:
    person.getPropertiesFiltered(null)

    then:
    thrown(AssertionError)
  }

  void "getPropertiesFiltered(Map) - for empty input list should return all properties"() {
    given:
    MyPogoPerson person = new MyPogoPerson(firstName: "myFirstName", lastName: "myLastName")

    when:
    Map<String, ?> filteredProperties = person.getPropertiesFiltered([])

    then:
    filteredProperties.size() == 3
    filteredProperties.collect({ it.key }).containsAll(["firstName", "lastName", "class"])
  }
}
