package org.klokwrk.lang.groovy.constructor.support

import org.klokwrk.lang.groovy.constructor.support.stub.TestPerson
import org.klokwrk.lang.groovy.constructor.support.stub.TestStrictPerson
import spock.lang.Specification
import spock.lang.Unroll

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
    thrown(IllegalArgumentException)
  }

  @Unroll
  void "should throw for invalid map constructor params [firstName: '#firstNameParam']"() {
    when:
    new TestPerson(firstName: firstNameParam)

    then:
    thrown(IllegalArgumentException)

    where:
    firstNameParam | _
    null           | _
    ""             | _
    "   "          | _
  }
}
