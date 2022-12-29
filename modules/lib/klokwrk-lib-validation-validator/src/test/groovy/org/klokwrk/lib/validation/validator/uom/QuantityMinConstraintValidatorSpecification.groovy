/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.validation.validator.uom

import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator
import org.klokwrk.lib.validation.constraint.uom.QuantityMinConstraint
import spock.lang.Shared
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.format.MeasurementParseException
import javax.measure.quantity.Mass
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class QuantityMinConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class QuantityInvalidTestObject_1 {
    @QuantityMinConstraint
    Quantity quantity
  }

  static class QuantityInvalidTestObject_2 {
    @QuantityMinConstraint(minQuantity = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_3 {
    @QuantityMinConstraint(minQuantity = "10 n/a")
    Quantity quantity
  }

  static class QuantityTestObject {
    @QuantityMinConstraint(minQuantity = "10.5 kg")
    Quantity untypedMassQuantityInclusive

    @QuantityMinConstraint(minQuantity = "10.5 kg", message = "Quantity is not valid.")
    Quantity untypedMassQuantityInclusiveWithCustomMessage

    @QuantityMinConstraint(minQuantity = "10.5 kg", acceptOnlyEqualUnit = true)
    Quantity untypedMassQuantityInclusiveEqualUnit

    @QuantityMinConstraint(minQuantity = "10.5 kg", inclusive = false)
    Quantity untypedMassQuantityExclusive

    @QuantityMinConstraint(minQuantity = "10.5 kg", inclusive = false, acceptOnlyEqualUnit = true)
    Quantity untypedMassQuantityExclusiveEqualUnit

    @QuantityMinConstraint(minQuantity = "-10.5 kg")
    Quantity<Mass> massQuantityInclusive

    @QuantityMinConstraint(minQuantity = "-10.5 kg", inclusive = false)
    Quantity<Mass> massQuantityExclusive
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
    constraintMapping.constraintDefinition(QuantityMinConstraint).validatedBy(QuantityMinConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should fail initialization when minQuantity is not specified"() {
    given:
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'minQuantity' parameter must be specified."
  }

  void "should fail initialization when the number in specified minQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

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
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'minQuantity' of '10 n/a' is not acceptable."
    assertionError.cause instanceof MeasurementParseException
    assertionError.cause.message == "Parse Error"
  }

  void "should validate inclusive minimum of a quantity"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam, massQuantityInclusive: massQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityInclusiveParam               | massQuantityInclusiveParam
    Quantities.getQuantity(10.5, Units.KILOGRAM)    | null
    Quantities.getQuantity(10.5000, Units.KILOGRAM) | null
    Quantities.getQuantity(10.5000, Units.KILOGRAM) | null
    Quantities.getQuantity(10.5001, Units.KILOGRAM) | null
    Quantities.getQuantity(11, Units.KILOGRAM)      | null
    Quantities.getQuantity(10_500, Units.GRAM)      | null
    Quantities.getQuantity(11_000, Units.GRAM)      | null

    null                                            | Quantities.getQuantity(-10.5, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(-10, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(-10_500, Units.GRAM)
    null                                            | Quantities.getQuantity(-10_000, Units.GRAM)
    null                                            | Quantities.getQuantity(-1, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(0, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(1, Units.KILOGRAM)

    Quantities.getQuantity(10.5, Units.KILOGRAM)    | Quantities.getQuantity(-10, Units.KILOGRAM)
    Quantities.getQuantity(11, Units.KILOGRAM)      | Quantities.getQuantity(-10_500, Units.GRAM)
    Quantities.getQuantity(10_500, Units.GRAM)      | Quantities.getQuantity(-10, Units.KILOGRAM)
    Quantities.getQuantity(11_000, Units.GRAM)      | Quantities.getQuantity(-10_500, Units.GRAM)
  }

  void "should fail validating inclusive minimum of a quantity"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMinConstraint.INVALID_QUANTITY_INCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveParam               | localeParam      | messageParam
    Quantities.getQuantity(10.4, Units.KILOGRAM)    | new Locale("en") | "Quantity '10.4 kg' must be greater than or equal to '10.5 kg'."
    Quantities.getQuantity(10.4000, Units.KILOGRAM) | new Locale("hr") | "Količina '10.4 kg' mora biti veća ili jednaka od '10.5 kg'."
    Quantities.getQuantity(10.4000, Units.KILOGRAM) | new Locale("en") | "Quantity '10.4 kg' must be greater than or equal to '10.5 kg'."
    Quantities.getQuantity(10.4001, Units.KILOGRAM) | new Locale("hr") | "Količina '10.4001 kg' mora biti veća ili jednaka od '10.5 kg'."
    Quantities.getQuantity(10, Units.GRAM)          | new Locale("en") | "Quantity '10 g' must be greater than or equal to '10.5 kg'."
    Quantities.getQuantity(10_400, Units.GRAM)      | new Locale("hr") | "Količina '10400 g' mora biti veća ili jednaka od '10.5 kg'."
  }

  void "should fail validating inclusive minimum of a quantity with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveWithCustomMessage: Quantities.getQuantity(10.4, Units.KILOGRAM))

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "Quantity is not valid."
      message == "Quantity is not valid."
    }
  }

  void "should validate inclusive minimum of a quantity with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveEqualUnit: untypedMassQuantityInclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityInclusiveWithEqualUnitParam  | _
    Quantities.getQuantity(10.5, Units.KILOGRAM)    | _
    Quantities.getQuantity(10.5000, Units.KILOGRAM) | _
    Quantities.getQuantity(10.5000, Units.KILOGRAM) | _
    Quantities.getQuantity(10.5001, Units.KILOGRAM) | _
    Quantities.getQuantity(11, Units.KILOGRAM)      | _
  }

  void "should fail validating inclusive minimum of a quantity with unequal unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveEqualUnit: untypedMassQuantityInclusiveWithEqualParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMinConstraint.INVALID_UNIT_EQUAL_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveWithEqualParam | localeParam      | messageParam
    Quantities.getQuantity(11_000, Units.GRAM) | new Locale("en") | "Unit symbol 'g' is not valid. The only supported symbol is 'kg'."
    Quantities.getQuantity(11_000, Units.GRAM) | new Locale("hr") | "Oznaka mjerne jedinice 'g' nije ispravna. Jedina podržana oznaka je 'kg'."
  }

  void "should fail validating inclusive minimum of a quantity with incompatible unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMinConstraint.INVALID_UNIT_COMPATIBLE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveParam       | localeParam      | messageParam
    Quantities.getQuantity(50, Units.METRE) | new Locale("en") | "Unit symbol 'm' is not valid. It is not compatible with expected 'kg'."
    Quantities.getQuantity(50, Units.METRE) | new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Oznaka mora biti kompatibilna sa 'kg'."
  }

  void "should validate exclusive minimum of a quantity"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusive: untypedMassQuantityExclusiveParam, massQuantityExclusive: massQuantityExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveParam               | massQuantityExclusiveParam
    Quantities.getQuantity(10.51, Units.KILOGRAM)   | null
    Quantities.getQuantity(10.5001, Units.KILOGRAM) | null
    Quantities.getQuantity(10.5001, Units.KILOGRAM) | null
    Quantities.getQuantity(10.5002, Units.KILOGRAM) | null
    Quantities.getQuantity(11, Units.KILOGRAM)      | null
    Quantities.getQuantity(10_501, Units.GRAM)      | null
    Quantities.getQuantity(11_000, Units.GRAM)      | null

    null                                            | Quantities.getQuantity(-10.49, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(-10, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(-10_490, Units.GRAM)
    null                                            | Quantities.getQuantity(-10_000, Units.GRAM)
    null                                            | Quantities.getQuantity(-1, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(0, Units.KILOGRAM)
    null                                            | Quantities.getQuantity(1, Units.KILOGRAM)

    Quantities.getQuantity(10.51, Units.KILOGRAM)   | Quantities.getQuantity(-10, Units.KILOGRAM)
    Quantities.getQuantity(11, Units.KILOGRAM)      | Quantities.getQuantity(-10_490, Units.GRAM)
    Quantities.getQuantity(10_501, Units.GRAM)      | Quantities.getQuantity(-10, Units.KILOGRAM)
    Quantities.getQuantity(11_000, Units.GRAM)      | Quantities.getQuantity(-10_499, Units.GRAM)
  }

  void "should fail validating exclusive minimum of a quantity"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusive: untypedMassQuantityExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMinConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityExclusiveParam            | localeParam      | messageParam
    Quantities.getQuantity(10.5, Units.KILOGRAM) | new Locale("en") | "Quantity '10.5 kg' must be strictly greater than '10.5 kg'."
    Quantities.getQuantity(10.5, Units.KILOGRAM) | new Locale("hr") | "Količina '10.5 kg' mora biti strogo veća od '10.5 kg'."
    Quantities.getQuantity(10_500, Units.GRAM)   | new Locale("en") | "Quantity '10500 g' must be strictly greater than '10.5 kg'."
    Quantities.getQuantity(10_500, Units.GRAM)   | new Locale("hr") | "Količina '10500 g' mora biti strogo veća od '10.5 kg'."
    Quantities.getQuantity(9, Units.KILOGRAM)    | new Locale("en") | "Quantity '9 kg' must be strictly greater than '10.5 kg'."
    Quantities.getQuantity(9, Units.KILOGRAM)    | new Locale("hr") | "Količina '9 kg' mora biti strogo veća od '10.5 kg'."
  }

  void "should validate exclusive minimum of a quantity with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusiveEqualUnit: untypedMassQuantityExclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveWithEqualUnitParam  | _
    Quantities.getQuantity(10.51, Units.KILOGRAM)   | _
    Quantities.getQuantity(10.5001, Units.KILOGRAM) | _
    Quantities.getQuantity(11, Units.KILOGRAM)      | _
  }

  void "should fail validating exclusive minimum of a quantity with equal unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusiveEqualUnit: untypedMassQuantityExclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMinConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityExclusiveWithEqualUnitParam | localeParam      | messageParam
    Quantities.getQuantity(10.5, Units.KILOGRAM)   | new Locale("en") | "Quantity '10.5 kg' must be strictly greater than '10.5 kg'."
    Quantities.getQuantity(10.5, Units.KILOGRAM)   | new Locale("hr") | "Količina '10.5 kg' mora biti strogo veća od '10.5 kg'."
    Quantities.getQuantity(10, Units.KILOGRAM)     | new Locale("en") | "Quantity '10 kg' must be strictly greater than '10.5 kg'."
    Quantities.getQuantity(10, Units.KILOGRAM)     | new Locale("hr") | "Količina '10 kg' mora biti strogo veća od '10.5 kg'."
  }
}
