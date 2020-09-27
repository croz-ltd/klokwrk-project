package org.klokwrk.lang.groovy.contracts.match

import spock.lang.Specification

import static org.hamcrest.Matchers.blankOrNullString
import static org.hamcrest.Matchers.emptyString
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not

class ContractsMatchSpecification extends Specification {
  String myTestClassString = """
      import static org.hamcrest.Matchers.blankOrNullString
      import static org.hamcrest.Matchers.not

      class MyTestClass {

        static class MyTestPerson {
          String firstName
          String lastName
        }

        void testInvalid() {
          MyTestPerson myTestPerson = new MyTestPerson(firstName: "First Name")
          requireMatch(myTestPerson.lastName, not(blankOrNullString()))
        }
      }
  """

  static class Person {
    String firstName
    String lastName
  }

  void "should throw for mismatch on parsed class"() {
    given:
    Class myTestClass = new GroovyClassLoader().parseClass(myTestClassString)
    def myTestClassInstance = myTestClass.newInstance([] as Object[])

    when:
    //noinspection GrUnresolvedAccess
    myTestClassInstance.testInvalid()

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: myTestPerson.lastName, expected: not(blankOrNullString()), actual: null]")
  }

  void "should throw for invalid matcher parameter"() {
    when:
    requireMatch("123", null)

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[condition: (matcher != null)]")
  }

  void "should throw for mismatch on constant"() {
    when:
    requireMatch("123", is(emptyString()))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: 123, expected: is(emptyString()), actual: 123]")
  }

  void "should throw for mismatch on method call"() {
    when:
    requireMatch("123".trim(), is(emptyString()))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: 123.trim(), expected: is(emptyString()), actual: 123]")
  }

  void "should throw for mismatch on object properties"() {
    given:
    Person person = new Person(firstName: "First Name")

    when:
    requireMatch(person.lastName, not(blankOrNullString()))

    then:
    AssertionError assertionError = thrown()
    assertionError.message.contains("[item: person.lastName, expected: not(blankOrNullString()), actual: null]")
  }

  void "should not throw when matching"() {
    when:
    requireMatch("123", not(emptyString()))

    then:
    true
  }
}
