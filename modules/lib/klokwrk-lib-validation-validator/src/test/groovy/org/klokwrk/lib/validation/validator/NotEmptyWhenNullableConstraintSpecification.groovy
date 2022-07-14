package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class NotEmptyWhenNullableConstraintSpecification extends Specification {
  @Shared
  Validator validator

  static class ConstraintTestObject {
    @NotEmptyWhenNullableConstraint
    List testList

    @NotEmptyWhenNullableConstraint
    Map testMap
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
    constraintMapping
        .constraintDefinition(NotEmptyWhenNullableConstraint)
        .validatedBy(NotEmptyWhenNullableConstraintForCollectionValidator)
        .validatedBy(NotEmptyWhenNullableConstraintForMapValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null values"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: null, testMap: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for non-empty collection"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: ["123"])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for non-empty map"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testMap: [key: "123"])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for non-empty list and non-empty map"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: ["123"], testMap: [key: "123"])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should fail for empty list"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: [])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == []
      it.propertyPath.toString() == "testList"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint.message}"
    }
  }

  void "should fail for empty map"() {
    given:
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testMap: [:])

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.invalidValue == [:]
      it.propertyPath.toString() == "testMap"
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint.message}"
    }
  }

  void "should correctly interpolate messages for various locales"() {
    given:
    Validator myValidator = configureValidator(localeParam)
    ConstraintTestObject constraintTestObject = new ConstraintTestObject(testList: [])

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(constraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      it.messageTemplate == "{org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint.message}"
      it.message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Must not be empty."
    new Locale("hr") | "Ne smije biti prazno."
  }
}