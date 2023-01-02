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

import groovy.transform.CompileStatic
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.klokwrk.lib.uom.format.KwrkSimpleUnitFormat
import org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint

import javax.measure.Quantity
import javax.measure.Unit
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@CompileStatic
class QuantityUnitConstraintValidator implements ConstraintValidator<QuantityUnitConstraint, Quantity> {
  String message
  String expectedUnitSymbol
  boolean acceptOnlyExactUnitSymbol
  List<String> compatibleUnitSymbolsForMessage

  Unit expectedUnit

  @Override
  void initialize(QuantityUnitConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()

    expectedUnitSymbol = constraintAnnotation.unitSymbol().trim()
    if (expectedUnitSymbol.isEmpty()) {
      throw new AssertionError("The 'unitSymbol' must be specified." as Object)
    }

    try {
      expectedUnit = KwrkSimpleUnitFormat.instance.parse(expectedUnitSymbol)
    }
    catch (RuntimeException e) { // codenarc-disable-line CatchRuntimeException
      throw new AssertionError("Specified 'unitSymbol' of '${ expectedUnitSymbol }' is not recognized.", e)
    }

    acceptOnlyExactUnitSymbol = constraintAnnotation.acceptOnlyExactUnitSymbol()
    compatibleUnitSymbolsForMessage = constraintAnnotation.compatibleUnitSymbolsForMessage().toList()
    if (compatibleUnitSymbolsForMessage.isEmpty()) {
      compatibleUnitSymbolsForMessage << expectedUnitSymbol
    }
  }

  @Override
  boolean isValid(Quantity quantity, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (quantity == null) {
      return true
    }

    if (acceptOnlyExactUnitSymbol) {
      if (quantity.unit == expectedUnit) {
        return true
      }
    }
    else {
      if (quantity.unit.isCompatible(expectedUnit)) {
        return true
      }
    }

    prepareConstraintValidatorContextForMessageInterpolation(quantity, context)
    return false
  }

  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  protected void prepareConstraintValidatorContextForMessageInterpolation(Quantity quantity, ConstraintValidatorContext context) {
    HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)
    if (message.isEmpty()) {
      // Prevent adding constraint violation with default message if it is empty
      context.disableDefaultConstraintViolation()

      hibernateContext.addExpressionVariable("expectedUnitSymbol", expectedUnitSymbol)
      hibernateContext.addExpressionVariable("providedUnitSymbol", quantity.unit.toString())
      hibernateContext.addExpressionVariable("compatibleUnitSymbols", compatibleUnitSymbolsForMessage.join(", "))

      hibernateContext.buildConstraintViolationWithTemplate("{${ QuantityUnitConstraint.INVALID_UNIT_SYMBOL_MESSAGE_KEY }}").enableExpressionLanguage().addConstraintViolation()
    }
    else {
      hibernateContext.addExpressionVariable("expectedUnitSymbol", expectedUnitSymbol)
      hibernateContext.addExpressionVariable("providedUnitSymbol", quantity.unit.toString())
      hibernateContext.addExpressionVariable("compatibleUnitSymbols", compatibleUnitSymbolsForMessage.join(", "))
      hibernateContext.buildConstraintViolationWithTemplate(message).enableExpressionLanguage().addConstraintViolation()
    }
  }
}
