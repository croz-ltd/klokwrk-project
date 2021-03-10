package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class UnLoCodeFormatConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class UnLoCodeFormatConstraintTestObject {
    @UnLoCodeFormatConstraint
    String unLoCodeString
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
    constraintMapping.constraintDefinition(UnLoCodeFormatConstraint).validatedBy(UnLoCodeFormatConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    UnLoCodeFormatConstraintTestObject unLoCodeFormatConstraintTestObject = new UnLoCodeFormatConstraintTestObject(unLoCodeString: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(unLoCodeFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty string"() {
    given:
    UnLoCodeFormatConstraintTestObject unLoCodeFormatConstraintTestObject = new UnLoCodeFormatConstraintTestObject(unLoCodeString: unLoCodeStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(unLoCodeFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    unLoCodeStringParam | _
    ""                  | _
    "    "              | _
  }

  void "should pass for string in unLoCode format"() {
    given:
    UnLoCodeFormatConstraintTestObject unLoCodeFormatConstraintTestObject = new UnLoCodeFormatConstraintTestObject(unLoCodeString: unLoCodeStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(unLoCodeFormatConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    unLoCodeStringParam | _
    "HRRJK"             | _
    "AAAAA"             | _
    "AAAA2"             | _
  }

  void "should fail for invalid unLoCode string"() {
    given:
    UnLoCodeFormatConstraintTestObject unLoCodeFormatConstraintTestObject = new UnLoCodeFormatConstraintTestObject(unLoCodeString: unLoCodeStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(unLoCodeFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == unLoCodeStringParam
      it.propertyPath.toString() == "unLoCodeString"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint.message}"
    }

    where:
    unLoCodeStringParam | _
    "HRRJK   "          | _
    "   HRRJK"          | _
    "aaaaa"             | _
    "AAAA1"             | _
    "A"                 | _
  }

  void "should correctly interpolate messages for various locales"() {
    given:
    Validator myValidator = configureValidator(localeParam)
    UnLoCodeFormatConstraintTestObject unLoCodeFormatConstraintTestObject = new UnLoCodeFormatConstraintTestObject(unLoCodeString: unLoCodeStringParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(unLoCodeFormatConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint.message}"
      it.message == messageParam
    }

    where:
    unLoCodeStringParam | localeParam      | messageParam
    "   HRRJK"          | new Locale("en") | "Invalid UN/LOCODE."
    "   HRRJK"          | new Locale("hr") | "Neispravan UN/LOCODE."
  }
}
