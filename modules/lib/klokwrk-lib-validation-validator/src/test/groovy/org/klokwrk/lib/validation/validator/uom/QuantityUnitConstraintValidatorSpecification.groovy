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
import org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint
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

class QuantityUnitConstraintValidatorSpecification extends Specification {
  @Shared
  Validator validator

  static class QuantityInvalidTestObject_1 {
    @QuantityUnitConstraint
    Quantity quantity1

    @QuantityUnitConstraint(exactUnitSymbol = "")
    Quantity quantity2

    @QuantityUnitConstraint(exactUnitSymbol = "  ")
    Quantity quantity3

    @QuantityUnitConstraint(compatibleUnitSymbols = [])
    Quantity quantity4

    @QuantityUnitConstraint(compatibleUnitSymbols = ["", "   "])
    Quantity quantity5

    @QuantityUnitConstraint(compatibleUnitSymbols = ["kg", "   "])
    Quantity quantity6

  }

  static class QuantityInvalidTestObject_2 {
    @QuantityUnitConstraint(exactUnitSymbol = "kg", compatibleUnitSymbols = ["kg", "g"])
    Quantity quantity1
  }

  static class QuantityInvalidTestObject_3 {
    @QuantityUnitConstraint(exactUnitSymbol = "n/a")
    Quantity quantity
  }

  static class QuantityInvalidTestObject_4 {
    @QuantityUnitConstraint(compatibleUnitSymbols = ["n/a", "kg", "g"])
    Quantity quantity
  }

  static class QuantityTestObject {
    @QuantityUnitConstraint(exactUnitSymbol = "kg")
    Quantity untypedQuantityExpectingExactUnitSymbol

    @QuantityUnitConstraint(
        message = 'Expected unit symbol is ${specifiedExactUnitSymbol}.',
        exactUnitSymbol = "kg"
    )
    Quantity untypedQuantityExpectingExactUnitSymbolWithCustomMessage

    @QuantityUnitConstraint(compatibleUnitSymbols = ["kg", "g"])
    Quantity untypedQuantityExpectingCompatibleUnitSymbol

    @QuantityUnitConstraint(
        message = 'Compatible unit symbols are: ${specifiedCompatibleUnitSymbols}.',
        compatibleUnitSymbols = ["kg", "g"]
    )
    Quantity untypedQuantityExpectingCompatibleUnitSymbolWithCustomMessage

    @QuantityUnitConstraint(exactUnitSymbol = "kg")
    Quantity<Mass> massQuantityExpectingExactUnitSymbol

    @QuantityUnitConstraint(compatibleUnitSymbols = ["kg", "g"])
    Quantity<Mass> massQuantityExpectingCompatibleUnitSymbol
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

  void "should fail initialization when none of exactUnitSymbol and compatibleUnitSymbols are specified"() {
    given:
    QuantityInvalidTestObject_1 invalidTestObject = new QuantityInvalidTestObject_1(
        quantity1: Quantities.getQuantity(10, Units.KILOGRAM),
        quantity2: Quantities.getQuantity(10, Units.KILOGRAM),
        quantity3: Quantities.getQuantity(10, Units.KILOGRAM),
        quantity4: Quantities.getQuantity(10, Units.KILOGRAM),
        quantity5: Quantities.getQuantity(10, Units.KILOGRAM),
        quantity6: Quantities.getQuantity(10, Units.KILOGRAM)
    )

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Either 'exactUnitSymbol' or 'compatibleUnitSymbols' have to be specified."
  }

  void "should fail initialization when both exactUnitSymbol and compatibleUnitSymbols are specified"() {
    given:
    QuantityInvalidTestObject_2 invalidTestObject = new QuantityInvalidTestObject_2(quantity1: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Only one of 'exactUnitSymbol' or 'compatibleUnitSymbols' can be specified."
  }

  void "should fail initialization when specified exactUnitySymbol is not recognized"() {
    given:
    QuantityInvalidTestObject_3 invalidTestObject = new QuantityInvalidTestObject_3(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "Specified 'exactUnitSymbol' of 'n/a' is not recognized."
    assertionError.cause instanceof MeasurementParseException
  }

  void "should fail initialization when the first specified compatibleUnitSymbols is not recognized"() {
    given:
    QuantityInvalidTestObject_4 invalidTestObject = new QuantityInvalidTestObject_4(quantity: Quantities.getQuantity(10, Units.KILOGRAM))

    when:
    validator.validate(invalidTestObject)

    then:
    AssertionError assertionError = thrown()
    assertionError.message == "The first specified unit symbol of 'n/a' in 'compatibleUnitSymbols' is not recognized."
    assertionError.cause instanceof MeasurementParseException
  }

  void "should validate quantity with correct exact unit symbol specified"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbol: untypedQuantityParam, massQuantityExpectingExactUnitSymbol: massQuantityParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedQuantityParam                       | massQuantityParam
    Quantities.getQuantity(10, Units.KILOGRAM) | null
    null                                       | Quantities.getQuantity(10, Units.KILOGRAM)
    Quantities.getQuantity(10, Units.KILOGRAM) | Quantities.getQuantity(10, Units.KILOGRAM)
  }

  void "should fail validating quantity with invalid exact unit symbol specified"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbol: Quantities.getQuantity(10, Units.METRE))

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityUnitConstraint.INVALID_EXACT_UNIT_SYMBOL_MESSAGE_KEY }}"
      message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Unit symbol must be 'kg'."
    new Locale("hr") | "Oznaka mjerne jedinice mora biti 'kg'."
  }

  void "should fail validating quantity with invalid exact unit symbol specified and with a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingExactUnitSymbolWithCustomMessage: Quantities.getQuantity(10, Units.METRE))

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == 'Expected unit symbol is ${specifiedExactUnitSymbol}.' // codenarc-disable-line GStringExpressionWithinString
      message == "Expected unit symbol is kg."
    }
  }

  void "should validate quantity with correct compatible unit symbol specified"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingCompatibleUnitSymbol: untypedQuantityParam, massQuantityExpectingCompatibleUnitSymbol: massQuantityParam)

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.isEmpty()

    where:
    untypedQuantityParam                       | massQuantityParam
    Quantities.getQuantity(10, Units.KILOGRAM) | null
    Quantities.getQuantity(10, Units.GRAM)     | null
    null                                       | Quantities.getQuantity(10, Units.KILOGRAM)
    null                                       | Quantities.getQuantity(10, Units.GRAM)
    Quantities.getQuantity(10, Units.KILOGRAM) | Quantities.getQuantity(10, Units.KILOGRAM)
    Quantities.getQuantity(10, Units.GRAM)     | Quantities.getQuantity(10, Units.KILOGRAM)
    Quantities.getQuantity(10, Units.KILOGRAM) | Quantities.getQuantity(10, Units.GRAM)
    Quantities.getQuantity(10, Units.GRAM)     | Quantities.getQuantity(10, Units.GRAM)
  }

  void "should fail validating quantity with invalid compatible unit symbol specified"() {
    given:
    Validator myValidator = configureValidator("klokwrkValidationConstraintMessages", localeParam)
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingCompatibleUnitSymbol: Quantities.getQuantity(10, Units.METRE))

    when:
    Set<ConstraintViolation> constraintViolations = myValidator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == "{${ QuantityUnitConstraint.INVALID_COMPATIBLE_UNIT_SYMBOL_MESSAGE_KEY }}"
      message == messageParam
    }

    where:
    localeParam      | messageParam
    new Locale("en") | "Unit symbol must be compatible with the following symbols: kg, g."
    new Locale("hr") | "Oznaka mjerne jedinice mora biti kompatibilna sa sljedećim jedinicama: kg, g."
  }

  void "should fail validating quantity with invalid compatible unit symbol specified and a custom message"() {
    given:
    QuantityTestObject testObject = new QuantityTestObject(untypedQuantityExpectingCompatibleUnitSymbolWithCustomMessage: Quantities.getQuantity(10, Units.METRE))

    when:
    Set<ConstraintViolation> constraintViolations = validator.validate(testObject)

    then:
    constraintViolations.size() == 1
    verifyAll(constraintViolations[0]) {
      messageTemplate == 'Compatible unit symbols are: ${specifiedCompatibleUnitSymbols}.' // codenarc-disable-line GStringExpressionWithinString
      message == "Compatible unit symbols are: kg, g."
    }
  }
}
