package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class NotBlankWhenNullableConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class ConstraintTestObject {
    @NotBlankWhenNullableConstraint
    String testString
  }

  void setupSpec() {
    validator = configureValidator()
  }

  private Validator configureValidator(Locale defaultLocale = Locale.default) {
    HibernateValidatorConfiguration configuration = Validation
        .byProvider(HibernateValidator)
        .configure()
        .messageInterpolator(
            new ResourceBundleMessageInterpolator(
                new PlatformResourceBundleLocator("klokwrkValidationConstraintMessages"), [new Locale("hr"), new Locale("en")] as Set<Locale>, defaultLocale, new DefaultLocaleResolver(), true
            )
        )

    ConstraintMapping constraintMapping = configuration.createConstraintMapping()
    constraintMapping.constraintDefinition(NotBlankWhenNullableConstraint).validatedBy(NotBlankWhenNullableConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testString: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for non-empty string"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testString: testStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    testStringParam | _
    "   123"        | _
    "   123   "     | _
    "123   "        | _
    "A"             | _
  }

  void "should fail for blank string"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testString: testStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == testStringParam
      it.propertyPath.toString() == "testString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint.message}"
    }

    where:
    testStringParam | _
    ""              | _
    "   "           | _
  }

  void "should correctly interpolate messages for various locales"() {
    given:
    Validator myValidator = configureValidator(localeParam)
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testString: testStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint.message}"
      it.message == messageParam
    }

    where:
    testStringParam | localeParam      | messageParam
    "   "           | new Locale("en") | "Must not be blank text."
    ""              | new Locale("hr") | "Ne smije biti prazan tekst."
  }
}
