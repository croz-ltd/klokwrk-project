/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
import org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint
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

class QuantityMaxConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class QuantityInvalidTestObject_1 {
    @QuantityMaxConstraint
    Quantity quantity
  }

  static class QuantityInvalidTestObject_2 {
    @QuantityMaxConstraint(maxQuantity = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_3 {
    @QuantityMaxConstraint(maxQuantity = "10 n/a")
    Quantity quantity
  }

  static class QuantityTestObject {
    @QuantityMaxConstraint(maxQuantity = "200.5 kg")
    Quantity untypedMassQuantityInclusive

    @QuantityMaxConstraint(maxQuantity = "200.5 kg", message = "Quantity is not valid.")
    Quantity untypedMassQuantityInclusiveWithCustomMessage

    @QuantityMaxConstraint(maxQuantity = "200.5 kg", acceptOnlyEqualUnit = true)
    Quantity untypedMassQuantityInclusiveEqualUnit

    @QuantityMaxConstraint(maxQuantity = "200.5 kg", inclusive = false)
    Quantity untypedMassQuantityExclusive

    @QuantityMaxConstraint(maxQuantity = "200.5 kg", inclusive = false, acceptOnlyEqualUnit = true)
    Quantity untypedMassQuantityExclusiveEqualUnit

    @QuantityMaxConstraint(maxQuantity = "-200.5 kg")
    Quantity<Mass> massQuantityInclusive

    @QuantityMaxConstraint(maxQuantity = "-200.5 kg", inclusive = false)
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
    constraintMapping.constraintDefinition(QuantityMaxConstraint).validatedBy(QuantityMaxConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should fail initialization when maxQuantity is not specified"() {
    given:
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'maxQuantity' parameter must be specified."
  }

  void "should fail initialization when the number in specified maxQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

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
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'maxQuantity' of '10 n/a' is not acceptable."
    assertionError.cause instanceof MeasurementParseException
    assertionError.cause.message == "Parse Error"
  }

  void "should validate inclusive maximum of a quantity"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam, massQuantityInclusive: massQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityInclusiveParam                | massQuantityInclusiveParam
    Quantities.getQuantity(200.5, Units.KILOGRAM)    | null
    Quantities.getQuantity(200.5000, Units.KILOGRAM) | null
    Quantities.getQuantity(200.4999, Units.KILOGRAM) | null
    Quantities.getQuantity(199, Units.KILOGRAM)      | null
    Quantities.getQuantity(200_500, Units.GRAM)      | null
    Quantities.getQuantity(199_000, Units.GRAM)      | null
    Quantities.getQuantity(-1, Units.KILOGRAM)       | null
    Quantities.getQuantity(0, Units.KILOGRAM)        | null
    Quantities.getQuantity(1, Units.KILOGRAM)        | null

    null                                             | Quantities.getQuantity(-200.5, Units.KILOGRAM)
    null                                             | Quantities.getQuantity(-201, Units.KILOGRAM)
    null                                             | Quantities.getQuantity(-200_500, Units.GRAM)
    null                                             | Quantities.getQuantity(-201_000, Units.GRAM)
    null                                             | Quantities.getQuantity(-201, Units.KILOGRAM)

    Quantities.getQuantity(200.5, Units.KILOGRAM)    | Quantities.getQuantity(-201, Units.KILOGRAM)
    Quantities.getQuantity(199, Units.KILOGRAM)      | Quantities.getQuantity(-200_500, Units.GRAM)
    Quantities.getQuantity(200_500, Units.GRAM)      | Quantities.getQuantity(-201.1, Units.KILOGRAM)
    Quantities.getQuantity(199_000, Units.GRAM)      | Quantities.getQuantity(-201_500, Units.GRAM)
  }

  void "should fail validating inclusive maximum of a quantity"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMaxConstraint.INVALID_QUANTITY_INCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveParam                | localeParam      | messageParam
    Quantities.getQuantity(200.6, Units.KILOGRAM)    | new Locale("en") | "Quantity '200.6 kg' must be less than or equal to '200.5 kg'."
    Quantities.getQuantity(200.5001, Units.KILOGRAM) | new Locale("hr") | "Količina '200.5001 kg' mora biti manja ili jednaka od '200.5 kg'."
    Quantities.getQuantity(200.6000, Units.KILOGRAM) | new Locale("en") | "Quantity '200.6 kg' must be less than or equal to '200.5 kg'."
    Quantities.getQuantity(201, Units.KILOGRAM)      | new Locale("hr") | "Količina '201 kg' mora biti manja ili jednaka od '200.5 kg'."
    Quantities.getQuantity(201_000, Units.GRAM)      | new Locale("en") | "Quantity '201000 g' must be less than or equal to '200.5 kg'."
    Quantities.getQuantity(200_600, Units.GRAM)      | new Locale("hr") | "Količina '200600 g' mora biti manja ili jednaka od '200.5 kg'."
  }

  void "should fail validating inclusive maximum of a quantity with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveWithCustomMessage: Quantities.getQuantity(201, Units.KILOGRAM))

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "Quantity is not valid."
      message == "Quantity is not valid."
    }
  }

  void "should validate inclusive maximum of a quantity with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveEqualUnit: untypedMassQuantityInclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityInclusiveWithEqualUnitParam   | _
    Quantities.getQuantity(100, Units.KILOGRAM)      | _
    Quantities.getQuantity(200, Units.KILOGRAM)      | _
    Quantities.getQuantity(200.5, Units.KILOGRAM)    | _
    Quantities.getQuantity(200.5000, Units.KILOGRAM) | _
    Quantities.getQuantity(200.5000, Units.KILOGRAM) | _
    Quantities.getQuantity(200.4999, Units.KILOGRAM) | _
  }

  void "should fail validating inclusive maximum of a quantity with unequal unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveEqualUnit: untypedMassQuantityInclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMaxConstraint.INVALID_UNIT_EQUAL_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveWithEqualUnitParam | localeParam      | messageParam
    Quantities.getQuantity(200_500, Units.GRAM)    | new Locale("en") | "Unit symbol 'g' is not valid. The only supported symbol is 'kg'."
    Quantities.getQuantity(200_500, Units.GRAM)    | new Locale("hr") | "Oznaka mjerne jedinice 'g' nije ispravna. Jedina podržana oznaka je 'kg'."
  }

  void "should fail validating inclusive maximum of a quantity with incompatible unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusive: untypedMassQuantityInclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMaxConstraint.INVALID_UNIT_COMPATIBLE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityInclusiveParam        | localeParam      | messageParam
    Quantities.getQuantity(100, Units.METRE) | new Locale("en") | "Unit symbol 'm' is not valid. It is not compatible with expected 'kg'."
    Quantities.getQuantity(100, Units.METRE) | new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Oznaka mora biti kompatibilna sa 'kg'."
  }

  void "should validate exclusive maximum of a quantity"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusive: untypedMassQuantityExclusiveParam, massQuantityExclusive: massQuantityExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveParam                | massQuantityExclusiveParam
    Quantities.getQuantity(200.49, Units.KILOGRAM)   | null
    Quantities.getQuantity(200.4999, Units.KILOGRAM) | null
    Quantities.getQuantity(200, Units.KILOGRAM)      | null
    Quantities.getQuantity(200_499, Units.GRAM)      | null
    Quantities.getQuantity(200_000, Units.GRAM)      | null
    Quantities.getQuantity(-1, Units.GRAM)           | null
    Quantities.getQuantity(0, Units.GRAM)            | null
    Quantities.getQuantity(1, Units.GRAM)            | null

    null                                             | Quantities.getQuantity(-200.51, Units.KILOGRAM)
    null                                             | Quantities.getQuantity(-201, Units.KILOGRAM)
    null                                             | Quantities.getQuantity(-200_510, Units.GRAM)
    null                                             | Quantities.getQuantity(-201_000, Units.GRAM)

    Quantities.getQuantity(200.49, Units.KILOGRAM)   | Quantities.getQuantity(-201, Units.KILOGRAM)
    Quantities.getQuantity(200, Units.KILOGRAM)      | Quantities.getQuantity(-200_510, Units.GRAM)
    Quantities.getQuantity(200_499, Units.GRAM)      | Quantities.getQuantity(-201, Units.KILOGRAM)
    Quantities.getQuantity(200_000, Units.GRAM)      | Quantities.getQuantity(-200_501, Units.GRAM)
  }

  void "should fail validating exclusive maximum of a quantity"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusive: untypedMassQuantityExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMaxConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityExclusiveParam             | localeParam      | messageParam
    Quantities.getQuantity(200.5, Units.KILOGRAM) | new Locale("en") | "Quantity '200.5 kg' must be strictly less than '200.5 kg'."
    Quantities.getQuantity(200.5, Units.KILOGRAM) | new Locale("hr") | "Količina '200.5 kg' mora biti strogo manja od '200.5 kg'."
    Quantities.getQuantity(200_500, Units.GRAM)   | new Locale("en") | "Quantity '200500 g' must be strictly less than '200.5 kg'."
    Quantities.getQuantity(200_500, Units.GRAM)   | new Locale("hr") | "Količina '200500 g' mora biti strogo manja od '200.5 kg'."
    Quantities.getQuantity(201, Units.KILOGRAM)   | new Locale("en") | "Quantity '201 kg' must be strictly less than '200.5 kg'."
    Quantities.getQuantity(201, Units.KILOGRAM)   | new Locale("hr") | "Količina '201 kg' mora biti strogo manja od '200.5 kg'."
  }

  void "should validate exclusive maximum of a quantity with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusiveEqualUnit: untypedMassQuantityExclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveWithEqualUnitParam   | _
    Quantities.getQuantity(200.49, Units.KILOGRAM)   | _
    Quantities.getQuantity(200.4999, Units.KILOGRAM) | _
    Quantities.getQuantity(200, Units.KILOGRAM)      | _
  }

  void 'should fail validating exclusive maximum of a quantity with unequal unit'() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusiveEqualUnit: untypedMassQuantityExclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityMaxConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY }}"
      message == messageParam
    }

    where:
    untypedMassQuantityExclusiveWithEqualUnitParam | localeParam      | messageParam
    Quantities.getQuantity(200.5, Units.KILOGRAM)  | new Locale("en") | "Quantity '200.5 kg' must be strictly less than '200.5 kg'."
    Quantities.getQuantity(200.5, Units.KILOGRAM)  | new Locale("hr") | "Količina '200.5 kg' mora biti strogo manja od '200.5 kg'."
    Quantities.getQuantity(201, Units.KILOGRAM)    | new Locale("en") | "Quantity '201 kg' must be strictly less than '200.5 kg'."
    Quantities.getQuantity(201, Units.KILOGRAM)    | new Locale("hr") | "Količina '201 kg' mora biti strogo manja od '200.5 kg'."
  }
}
