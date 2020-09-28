package org.klokwrk.lang.groovy.constructor.support

import org.klokwrk.lang.groovy.constructor.support.stub.TestPerson
import org.klokwrk.lang.groovy.constructor.support.stub.TestStrictPerson
import spock.lang.Specification

class PostMapConstructorCheckableSpecification extends Specification {
  void "should not throw for empty constructor by default"() {
    when:
    new TestPerson()

    then:
    true
  }

  void "should throw for empty constructor when postMapConstructorShouldThrowForEmptyConstructorArguments() is overridden"() {
    when:
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
}
