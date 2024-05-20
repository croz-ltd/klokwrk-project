/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.lo.validation.validator.uom

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint
import spock.lang.Shared
import spock.lang.Specification

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import javax.measure.Quantity
import javax.measure.format.MeasurementParseException

class QuantityRangeConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class QuantityInvalidTestObject_1 {
    @QuantityRangeConstraint
    Quantity quantity
  }

  static class QuantityInvalidTestObject_2 {
    @QuantityRangeConstraint(minQuantity = "10 kg")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_3 {
    @QuantityRangeConstraint(minQuantity = "n/a", maxQuantity = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_4 {
    @QuantityRangeConstraint(minQuantity = "10 n/a", maxQuantity = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_5 {
    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_6 {
    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_7 {
    @QuantityRangeConstraint(minQuantity = "10000 g", maxQuantity = "100 kg", acceptOnlyEqualUnit = true)
    Quantity quantity
  }

  static class QuantityInvalidTestObject_8 {
    @QuantityRangeConstraint(minQuantity = "10 m", maxQuantity = "100 kg")
    Quantity quantity
  }

  static class QuantityTestObject {
    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg")
    Quantity quantityMinAndMaxInclusive

    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg", message = "Quantity is not valid.")
    Quantity quantityMinAndMaxInclusiveWithCustomMessage

    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg", acceptOnlyEqualUnit = true)
    Quantity quantityMinAndMaxInclusiveWithEqualUnit

    @QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg", maxInclusive = false)
    Quantity quantityMinInclusiveAndMaxExclusive

    @QuantityRangeConstraint(minQuantity = "10 kg", minInclusive = false, maxQuantity = "100 kg")
    Quantity quantityMinExclusiveAndMaxInclusive

    @QuantityRangeConstraint(minQuantity = "10 kg", minInclusive = false, maxQuantity = "100 kg", maxInclusive = false)
    Quantity quantityMinExclusiveAndMaxExclusive
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
    constraintMapping.constraintDefinition(QuantityRangeConstraint).validatedBy(QuantityRangeConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should fail initialization when minQuantity is not specified"() {
    given:
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'minQuantity' parameter must be specified."
  }

  void "should fail initialization when maxQuantity is not specified"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'maxQuantity' parameter must be specified."
  }

  void "should fail initialization when the number in specified minQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'minQuantity' of 'n/a' is not acceptable."
    assertionError.cause instanceof IllegalArgumentException
    assertionError.cause.message == "Number cannot be parsed"
  }

  void "should fail initialization when the unit in specified minQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_4 invalidTestObject = new QuantityInvalidTestObject_4(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'minQuantity' of '10 n/a' is not acceptable."
    assertionError.cause instanceof MeasurementParseException
    assertionError.cause.message == "Parse Error"
  }

  void "should fail initialization when the number in specified maxQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_5 invalidTestObject = new QuantityInvalidTestObject_5(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'maxQuantity' of 'n/a' is not acceptable."
    assertionError.cause instanceof IllegalArgumentException
    assertionError.cause.message == "Number cannot be parsed"
  }

  void "should fail initialization when the unit in specified maxQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_6 invalidTestObject = new QuantityInvalidTestObject_6(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'maxQuantity' of '100 n/a' is not acceptable."
    assertionError.cause instanceof MeasurementParseException
    assertionError.cause.message == "Parse Error"
  }

  void "should fail initialization when acceptOnlyEqualUnitParameter is set to true, but units of min and max quantities are different"() {
    given:
    QuantityInvalidTestObject_7 invalidTestObject = new QuantityInvalidTestObject_7(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "When 'acceptOnlyEqualUnit' is set to 'true', both min and max quantities must use equal units."
  }

  void "should fail initialization when units of minQuantity and maxQuantity are not mutually compatible"() {
    given:
    QuantityInvalidTestObject_8 invalidTestObject = new QuantityInvalidTestObject_8(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The units of 'minQuantity' and 'maxQuantity' must be mutually compatible."
  }

  void "should validate inclusive range"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusive: quantityMinAndMaxInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    quantityMinAndMaxInclusiveParam | _
    10.kg                           | _
    10_000.g                        | _
    100.kg                          | _
    100_000.g                       | _
  }

  void "should fail validating inclusive range"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusive: quantityMinAndMaxInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_QUANTITY_MIN_INCLUSIVE_MAX_INCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    quantityMinAndMaxInclusiveParam | localeParam      | messageParam
    1.kg                            | new Locale("en") | "Quantity '1 kg' must be greater than or equal to '10 kg' and less than or equal to '100 kg'."
    101.kg                          | new Locale("hr") | "Količina '101 kg' mora biti veća ili jednaka od '10 kg' i manja ili jednaka od '100 kg'."
    1_000.g                         | new Locale("en") | "Quantity '1000 g' must be greater than or equal to '10 kg' and less than or equal to '100 kg'."
    101_000.g                       | new Locale("hr") | "Količina '101000 g' mora biti veća ili jednaka od '10 kg' i manja ili jednaka od '100 kg'."
  }

  void "should fail validating inclusive range with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusiveWithCustomMessage: 101.kg)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "Quantity is not valid."
      message == "Quantity is not valid."
    }
  }

  void "should validate inclusive range with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusiveWithEqualUnit: quantityMinAndMaxInclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    quantityMinAndMaxInclusiveWithEqualUnitParam | _
    10.kg                                        | _
    100.kg                                       | _
  }

  void "should fail validating inclusive range with unequal unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusiveWithEqualUnit: quantityMinAndMaxInclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_UNIT_EQUAL_KEY }}"
      message == messageParam
    }

    where:
    quantityMinAndMaxInclusiveWithEqualUnitParam | localeParam      | messageParam
    100_000.g                                    | new Locale("en") | "Unit symbol 'g' is not valid. The only supported symbol is 'kg'."
    10_000.g                                     | new Locale("hr") | "Oznaka mjerne jedinice 'g' nije ispravna. Jedina podržana oznaka je 'kg'."
  }

  void "should fail validating incompatible unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinAndMaxInclusive: quantityMinAndMaxInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_UNIT_COMPATIBLE_KEY }}"
      message == messageParam
    }

    where:
    quantityMinAndMaxInclusiveParam | localeParam      | messageParam
    50.m                            | new Locale("en") | "Unit symbol 'm' is not valid. It is not compatible with expected 'kg'."
    50.m                            | new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Oznaka mora biti kompatibilna sa 'kg'."
  }

  void "should validate min inclusive and max exclusive range"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinInclusiveAndMaxExclusive: quantityMinInclusiveAndMaxExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    quantityMinInclusiveAndMaxExclusiveParam | _
    10.kg                                    | _
    99.9999.kg                               | _
  }

  void "should fail validating min inclusive and max exclusive range"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinInclusiveAndMaxExclusive: quantityMinInclusiveAndMaxExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_QUANTITY_MIN_INCLUSIVE_MAX_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    quantityMinInclusiveAndMaxExclusiveParam | localeParam      | messageParam
    1.kg                                     | new Locale("en") | "Quantity '1 kg' must be greater than or equal to '10 kg' and strictly less than '100 kg'."
    100.kg                                   | new Locale("hr") | "Količina '100 kg' mora biti veća ili jednaka od '10 kg' i strogo manja od '100 kg'."
    100_000.g                                | new Locale("en") | "Quantity '100000 g' must be greater than or equal to '10 kg' and strictly less than '100 kg'."
    101.kg                                   | new Locale("hr") | "Količina '101 kg' mora biti veća ili jednaka od '10 kg' i strogo manja od '100 kg'."
  }

  void "should validate min exclusive and max inclusive range"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinExclusiveAndMaxInclusive: quantityMinExclusiveAndMaxInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    quantityMinExclusiveAndMaxInclusiveParam | _
    10.001.kg                                | _
    100.kg                                   | _
  }

  void "should fail validating min exclusive and max inclusive range"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinExclusiveAndMaxInclusive: quantityMinExclusiveAndMaxInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_INCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    quantityMinExclusiveAndMaxInclusiveParam | localeParam      | messageParam
    10.kg                                    | new Locale("en") | "Quantity '10 kg' must be strictly greater than '10 kg' and less than or equal to '100 kg'."
    10_000.g                                 | new Locale("hr") | "Količina '10000 g' mora biti strogo veća od '10 kg' i manja ili jednaka od '100 kg'."
    101_000.g                                | new Locale("en") | "Quantity '101000 g' must be strictly greater than '10 kg' and less than or equal to '100 kg'."
    101.kg                                   | new Locale("hr") | "Količina '101 kg' mora biti strogo veća od '10 kg' i manja ili jednaka od '100 kg'."
  }

  void "should validate min exclusive and max exclusive range"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(quantityMinExclusiveAndMaxExclusive: quantityMinExclusiveAndMaxExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    quantityMinExclusiveAndMaxExclusiveParam | _
    10.001.kg                                | _
    99.999.kg                                | _
  }

  void "should fail validating min exclusive and max exclusive range"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(quantityMinExclusiveAndMaxExclusive: quantityMinExclusiveAndMaxExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityRangeConstraint.INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    quantityMinExclusiveAndMaxExclusiveParam | localeParam      | messageParam
    10.kg                                    | new Locale("en") | "Quantity '10 kg' must be strictly greater than '10 kg' and strictly less than '100 kg'."
    10_000.g                                 | new Locale("hr") | "Količina '10000 g' mora biti strogo veća od '10 kg' i strogo manja od '100 kg'."
    100_000.g                                | new Locale("en") | "Quantity '100000 g' must be strictly greater than '10 kg' and strictly less than '100 kg'."
    100.kg                                   | new Locale("hr") | "Količina '100 kg' mora biti strogo veća od '10 kg' i strogo manja od '100 kg'."
  }
}
