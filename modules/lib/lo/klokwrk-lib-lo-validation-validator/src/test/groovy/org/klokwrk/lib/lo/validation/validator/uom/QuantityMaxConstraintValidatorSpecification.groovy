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
import org.klokwrk.lib.lo.validation.constraint.uom.QuantityMaxConstraint
import spock.lang.Shared
import spock.lang.Specification

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import javax.measure.Quantity
import javax.measure.format.MeasurementParseException
import javax.measure.quantity.Mass

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
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'maxQuantity' parameter must be specified."
  }

  void "should fail initialization when the number in specified maxQuantity parameter cannot be parsed"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity: 10.kg)

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
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: 10.kg)

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
    untypedMassQuantityInclusiveParam | massQuantityInclusiveParam
    200.5.kg                          | null
    200.5000.kg                       | null
    200.4999.kg                       | null
    199.kg                            | null
    200_500.g                         | null
    199_000.g                         | null
    -1.kg                             | null
    0.kg                              | null
    1.kg                              | null

    null                              | -200.5.kg
    null                              | -201.kg
    null                              | -200_500.g
    null                              | -201_000.g
    null                              | -201.kg

    200.5.kg                          | -201.kg
    199.kg                            | -200_500.g
    200_500.g                         | -201.1.kg
    199_000.g                         | -201_500.g
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
    untypedMassQuantityInclusiveParam | localeParam      | messageParam
    200.6.kg                          | new Locale("en") | "Quantity '200.6 kg' must be less than or equal to '200.5 kg'."
    200.5001.kg                       | new Locale("hr") | "Količina '200.5001 kg' mora biti manja ili jednaka od '200.5 kg'."
    200.6000.kg                       | new Locale("en") | "Quantity '200.6 kg' must be less than or equal to '200.5 kg'."
    201.kg                            | new Locale("hr") | "Količina '201 kg' mora biti manja ili jednaka od '200.5 kg'."
    201_000.g                         | new Locale("en") | "Quantity '201000 g' must be less than or equal to '200.5 kg'."
    200_600.g                         | new Locale("hr") | "Količina '200600 g' mora biti manja ili jednaka od '200.5 kg'."
  }

  void "should fail validating inclusive maximum of a quantity with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityInclusiveWithCustomMessage: 201.kg)

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
    untypedMassQuantityInclusiveWithEqualUnitParam | _
    100.kg                                         | _
    200.kg                                         | _
    200.5.kg                                       | _
    200.5000.kg                                    | _
    200.5000.kg                                    | _
    200.4999.kg                                    | _
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
    200_500.g                                      | new Locale("en") | "Unit symbol 'g' is not valid. The only supported symbol is 'kg'."
    200_500.g                                      | new Locale("hr") | "Oznaka mjerne jedinice 'g' nije ispravna. Jedina podržana oznaka je 'kg'."
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
    untypedMassQuantityInclusiveParam | localeParam      | messageParam
    100.m                             | new Locale("en") | "Unit symbol 'm' is not valid. It is not compatible with expected 'kg'."
    100.m                             | new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Oznaka mora biti kompatibilna sa 'kg'."
  }

  void "should validate exclusive maximum of a quantity"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusive: untypedMassQuantityExclusiveParam, massQuantityExclusive: massQuantityExclusiveParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveParam | massQuantityExclusiveParam
    200.49.kg                         | null
    200.4999.kg                       | null
    200.kg                            | null
    200_499.g                         | null
    200_000.g                         | null
    -1.g                              | null
    0.g                               | null
    1.g                               | null

    null                              | -200.51.kg
    null                              | -201.kg
    null                              | -200_510.g
    null                              | -201_000.g

    200.49.kg                         | -201.kg
    200.kg                            | -200_510.g
    200_499.g                         | -201.kg
    200_000.g                         | -200_501.g
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
    untypedMassQuantityExclusiveParam | localeParam      | messageParam
    200.5.kg                          | new Locale("en") | "Quantity '200.5 kg' must be strictly less than '200.5 kg'."
    200.5.kg                          | new Locale("hr") | "Količina '200.5 kg' mora biti strogo manja od '200.5 kg'."
    200_500.g                         | new Locale("en") | "Quantity '200500 g' must be strictly less than '200.5 kg'."
    200_500.g                         | new Locale("hr") | "Količina '200500 g' mora biti strogo manja od '200.5 kg'."
    201.kg                            | new Locale("en") | "Quantity '201 kg' must be strictly less than '200.5 kg'."
    201.kg                            | new Locale("hr") | "Količina '201 kg' mora biti strogo manja od '200.5 kg'."
  }

  void "should validate exclusive maximum of a quantity with equal unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedMassQuantityExclusiveEqualUnit: untypedMassQuantityExclusiveWithEqualUnitParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedMassQuantityExclusiveWithEqualUnitParam | _
    200.49.kg                                      | _
    200.4999.kg                                    | _
    200.kg                                         | _
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
    200.5.kg                                       | new Locale("en") | "Quantity '200.5 kg' must be strictly less than '200.5 kg'."
    200.5.kg                                       | new Locale("hr") | "Količina '200.5 kg' mora biti strogo manja od '200.5 kg'."
    201.kg                                         | new Locale("en") | "Quantity '201 kg' must be strictly less than '200.5 kg'."
    201.kg                                         | new Locale("hr") | "Količina '201 kg' mora biti strogo manja od '200.5 kg'."
  }
}
