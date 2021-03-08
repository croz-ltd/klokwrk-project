package org.klokwrk.lib.validation.springboot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@SpringBootTest
class ValidationServiceDefaultSetupSpecification extends Specification {
  @Autowired
  ValidationService validationService

  static class TestObject {
    @NotNull
    @Size(min = 1, max = 15)
    String stringProperty
  }

  void "default configuration should be applied"() {
    expect:
    validationService.enabled
    validationService.messageSourceBaseNames == ["klokwrkValidationHibernateMessages"] as String[]
  }

  void "should not throw for valid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: "bla")

    when:
    validationService.validate(testObject)

    then:
    true
  }

  void "should throw for invalid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: null)

    when:
    validationService.validate(testObject)

    then:
    thrown(ConstraintViolationException)
  }
}
