package org.klokwrk.lib.validation.validator

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class ValueOfEnumConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static enum TestEnum {
    TEST_ONE,
    TEST_TWO,
    TEST_THREE
  }

  static class ValueOfEnumConstraintTestObject {
    @ValueOfEnumConstraint(enumClass = TestEnum)
    String firstEnumName

    @ValueOfEnumConstraint(enumClass = TestEnum, enumNamesSubset = ["test_one", "test_two"])
    String secondEnumName
  }

  static class ValueOfEnumConstraintTestObjectWithCustomMessageTemplates {
    @ValueOfEnumConstraint(enumClass = TestEnum, message = "{custom.ValueOfEnumConstraint.invalidEnumValueMessage}")
    String firstEnumName

    @ValueOfEnumConstraint(enumClass = TestEnum, enumNamesSubset = ["test_one", "test_two"], message = "{custom.ValueOfEnumConstraint.invalidSubsetOfEnumMessage}")
    String secondEnumName
  }

  static class ValueOfEnumConstraintTestObjectWithCustomMessages {
    @ValueOfEnumConstraint(enumClass = TestEnum, message = 'This is invalid enum value - [enumClassSimpleName: ${enumClassSimpleName}, enumNamesSubsetList: ${enumNamesSubsetList}].')
    String firstEnumName

    @ValueOfEnumConstraint(
        enumClass = TestEnum, enumNamesSubset = ["test_one", "test_two"],
        message = 'This is invalid subset of enum - [enumClassSimpleName: ${enumClassSimpleName}, enumNamesSubsetList: ${enumNamesSubsetList}].'
    )
    String secondEnumName
  }

  static class ValueOfEnumConstraintTestObjectInvalid {
    @ValueOfEnumConstraint(enumClass = TestEnum, enumNamesSubset = ["one", "two"])
    String enumName
  }

  void setupSpec() {
    validator = configureValidator("klokwrkValidationConstraintMessages")
  }

  private Validator configureValidator(String resourceBundleName, Locale defaultLocale = Locale.default) {
    HibernateValidatorConfiguration configuration = Validation
        .byProvider(HibernateValidator)
        .configure()
        .messageInterpolator(
            new ResourceBundleMessageInterpolator(
                new PlatformResourceBundleLocator(resourceBundleName), [new Locale("hr"), new Locale("en")] as Set<Locale>, defaultLocale, new DefaultLocaleResolver(), true
            )
        )

    ConstraintMapping constraintMapping = configuration.createConstraintMapping()
    constraintMapping.constraintDefinition(ValueOfEnumConstraint).validatedBy(ValueOfEnumConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should pass for null string"() {
    given:
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: null, secondEnumName: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.isEmpty()
  }

  void "should pass for empty string"() {
    given:
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: enumNameParam, secondEnumName: enumNameParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    enumNameParam | _
    ""            | _
    "    "        | _
  }

  void "should pass for valid enum names"() {
    given:
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: enumNameParam, secondEnumName: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    enumNameParam | _
    "test_one"    | _
    "test_two"    | _
    "test_three"  | _

    "TEST_ONE"    | _
    "TEST_TWO"    | _
    "TEST_THREE"  | _

    "teST_One"    | _
    "TEst_tWo"    | _
    "TEST_three"  | _
  }

  void "should pass for valid subset of enum names"() {
    given:
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: null, secondEnumName: enumNameParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.isEmpty()

    where:
    enumNameParam | _
    "test_one"    | _
    "test_two"    | _

    "TEST_ONE"    | _
    "TEST_TWO"    | _

    "teST_One"    | _
    "TEst_tWo"    | _
  }

  void "should fail for invalid configuration of subset"() {
    given:
    ValueOfEnumConstraintTestObjectInvalid valueOfEnumConstraintTestObjectInvalid = new ValueOfEnumConstraintTestObjectInvalid(enumName: null)

    when:
    validator.validate(valueOfEnumConstraintTestObjectInvalid)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == 'Subset value of \'ONE\' is not part of org.klokwrk.lib.validation.validator.ValueOfEnumConstraintValidatorSpecification$TestEnum enum.'
  }

  void "should correctly interpolate default messages for different locales when invalid enum name is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: firstEnumNameParam, secondEnumName: null)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint.invalidEnumValueMessage}"
      message == messageParam
    }

    where:
    firstEnumNameParam | localeParam      | messageParam
    "invalid"          | new Locale("en") | "Must be a value from TestEnum enum."
    "invalid"          | new Locale("hr") | "Mora biti vrijednost iz TestEnum enumeracije."
  }

  void "should correctly interpolate default messages for different locales when invalid subset of enum names is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    ValueOfEnumConstraintTestObject valueOfEnumConstraintTestObject = new ValueOfEnumConstraintTestObject(firstEnumName: null, secondEnumName: secondEnumNameParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(valueOfEnumConstraintTestObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint.invalidSubsetOfEnumMessage}"
      message == messageParam
    }

    where:
    secondEnumNameParam | localeParam      | messageParam
    "invalid"           | new Locale("en") | "Must be any of the following values: TEST_ONE, TEST_TWO."
    "test_three"        | new Locale("en") | "Must be any of the following values: TEST_ONE, TEST_TWO."
    "TEST_THREE"        | new Locale("en") | "Must be any of the following values: TEST_ONE, TEST_TWO."
    "teST_ThReE"        | new Locale("en") | "Must be any of the following values: TEST_ONE, TEST_TWO."

    "invalid"           | new Locale("hr") | "Mora biti jedna od slijedećih vrijednosti: TEST_ONE, TEST_TWO."
    "test_three"        | new Locale("hr") | "Mora biti jedna od slijedećih vrijednosti: TEST_ONE, TEST_TWO."
    "TEST_THREE"        | new Locale("hr") | "Mora biti jedna od slijedećih vrijednosti: TEST_ONE, TEST_TWO."
    "teST_ThReE"        | new Locale("hr") | "Mora biti jedna od slijedećih vrijednosti: TEST_ONE, TEST_TWO."
  }

  void "should correctly interpolate custom message templates with expressions for different locales when invalid enum name is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintTestMessages", localeParam)
    ValueOfEnumConstraintTestObjectWithCustomMessageTemplates testObject = new ValueOfEnumConstraintTestObjectWithCustomMessageTemplates(firstEnumName: firstEnumNameParam, secondEnumName: null)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{custom.ValueOfEnumConstraint.invalidEnumValueMessage}"
      message == messageParam
    }

    where:
    firstEnumNameParam | localeParam      | messageParam
    "invalid"          | new Locale("en") | "Must be of TestEnum type - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO, TEST_THREE]."
    "invalid"          | new Locale("hr") | "Mora biti TestEnum - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO, TEST_THREE]."
  }

  void "should correctly interpolate custom message templates with expressions for different locales when invalid subset of enum names is validated"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintTestMessages", localeParam)
    ValueOfEnumConstraintTestObjectWithCustomMessageTemplates testObject = new ValueOfEnumConstraintTestObjectWithCustomMessageTemplates(firstEnumName: null, secondEnumName: secondEnumNameParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{custom.ValueOfEnumConstraint.invalidSubsetOfEnumMessage}"
      message == messageParam
    }

    where:
    secondEnumNameParam | localeParam      | messageParam
    "invalid"           | new Locale("en") | "Must be one of the following values: TEST_ONE or TEST_TWO - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO]."
    "invalid"           | new Locale("hr") | "Mora biti jedna od slijedećih vrijednosti: TEST_ONE ili TEST_TWO - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO]."
  }

  void "should use custom annotation configured message with expressions when invalid enum name is validated"() {
    given:
    ValueOfEnumConstraintTestObjectWithCustomMessages testObject = new ValueOfEnumConstraintTestObjectWithCustomMessages(firstEnumName: "invalid", secondEnumName: null)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      message == "This is invalid enum value - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO, TEST_THREE]."
    }
  }

  void "should use custom annotation configured message with expressions when invalid subset of enum names is validated"() {
    given:
    ValueOfEnumConstraintTestObjectWithCustomMessages testObject = new ValueOfEnumConstraintTestObjectWithCustomMessages(firstEnumName: null, secondEnumName: "invalid")

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      message == "This is invalid subset of enum - [enumClassSimpleName: TestEnum, enumNamesSubsetList: TEST_ONE, TEST_TWO]."
    }
  }
}
