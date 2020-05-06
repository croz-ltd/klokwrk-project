package net.croz.cargotracker.lang.groovy.extension

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
