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
import org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint
import spock.lang.Shared
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.format.MeasurementParseException
import javax.measure.quantity.Mass
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class QuantityUnitConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class QuantityInvalidTestObject_1 {
    @QuantityUnitConstraint
    Quantity quantity
  }

  static class QuantityInvalidTestObject_2 {
    @QuantityUnitConstraint(unitSymbol = "")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_3 {
    @QuantityUnitConstraint(unitSymbol = "n/a")
    Quantity quantity
  }

  static class QuantityTestObject {
    @QuantityUnitConstraint(unitSymbol = "kg")
    Quantity untypedQuantity

    @QuantityUnitConstraint(unitSymbol = "kg", message = 'Expected unit symbol is ${expectedUnitSymbol}.')
    Quantity untypedQuantityWithCustomMessage

    @QuantityUnitConstraint(unitSymbol = "kg", compatibleUnitSymbolsForMessage = ["kg", "g", "bla", "ble"])
    Quantity untypedQuantityWithCompatibleUnitSymbolsSpecified

    @QuantityUnitConstraint(unitSymbol = "kg", acceptOnlyExactUnitSymbol = true)
    Quantity untypedQuantityExpectingExactUnitSymbol

    @QuantityUnitConstraint(unitSymbol = "kg", acceptOnlyExactUnitSymbol = true, message = 'Unit symbol must be ${expectedUnitSymbol}.')
    Quantity untypedQuantityExpectingExactUnitSymbolWithCustomMessage

    @QuantityUnitConstraint(unitSymbol = "kg")
    Quantity<Mass> massQuantity

    @QuantityUnitConstraint(unitSymbol = "kg", acceptOnlyExactUnitSymbol = true)
    Quantity<Mass> massQuantityExpectingExactUnitSymbol
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
    constraintMapping.constraintDefinition(QuantityUnitConstraint).validatedBy(QuantityUnitConstraintValidator)

    ValidatorFactory validatorFactory = configuration.addMapping(constraintMapping).buildValidatorFactory()
    return validatorFactory.getValidator()
  }

  void "should fail initialization when unit symbol is not specified"() {
    given:
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'unitSymbol' must be specified."
  }

  void "should fail initialization when specified unit symbol is empty"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The 'unitSymbol' must be specified."
  }

  void "should fail initialization when specified unit symbol cannot be parsed"() {
    given:
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: 10.kg)

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'unitSymbol' of 'n/a' is not recognized."
    assertionError.cause instanceof MeasurementParseException
  }

  void "should validate quantity with correct compatible unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantity: untypedQuantityParam, massQuantity: massQuantityParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedQuantityParam | massQuantityParam
    10.kg                | null
    10.g                 | null
    null                 | 10.kg
    null                 | 10.g
    10.kg                | 10.kg
    10.kg                | 10.g
    10.g                 | 10.kg
    10.g                 | 10.g
  }

  void "should fail validating quantity with invalid unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantity: 10.m)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityUnitConstraint.INVALID_UNIT_SYMBOL_MESSAGE_KEY }}"
      message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Unit symbol 'm' is not valid. Supported symbols are: kg."
    new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Podržane oznake su: kg."
  }

  void "should fail validating quantity with invalid unit and compatibleUnitSymbolsForMessage specified"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityWithCompatibleUnitSymbolsSpecified: 10.m)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityUnitConstraint.INVALID_UNIT_SYMBOL_MESSAGE_KEY }}"
      message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Unit symbol 'm' is not valid. Supported symbols are: kg, g, bla, ble."
    new Locale("hr") | "Oznaka mjerne jedinice 'm' nije ispravna. Podržane oznake su: kg, g, bla, ble."
  }

  void "should fail validating quantity with invalid unit and with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityWithCustomMessage: 10.m)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == 'Expected unit symbol is ${expectedUnitSymbol}.' // codenarc-disable-line GStringExpressionWithinString
      message == "Expected unit symbol is kg."
    }
  }

  void "should validate quantity with correct exact unit"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbol: untypedQuantityParam, massQuantityExpectingExactUnitSymbol: massQuantityParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedQuantityParam | massQuantityParam
    10.kg                | null
    null                 | 10.kg
    10.kg                | 10.kg
  }

  void "should fail validating quantity with exact unit"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbol: 10.g)

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityUnitConstraint.INVALID_UNIT_SYMBOL_MESSAGE_KEY }}"
      message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Unit symbol 'g' is not valid. Supported symbols are: kg."
    new Locale("hr") | "Oznaka mjerne jedinice 'g' nije ispravna. Podržane oznake su: kg."
  }

  void "should fail validating quantity with exact unit and a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbolWithCustomMessage: 10.g)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == 'Unit symbol must be ${expectedUnitSymbol}.' // codenarc-disable-line GStringExpressionWithinString
      message == "Unit symbol must be kg."
    }
  }
}
