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

  String exactUnitSymbol
  Unit exactUnit

  List<String> compatibleUnitSymbols
  Unit firstCompatibleUnit

  boolean isExactUnitSymbolSpecified
  boolean areCompatibleUnitSymbolsSpecified

  @Override
  void initialize(QuantityUnitConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()

    exactUnitSymbol = constraintAnnotation.exactUnitSymbol().trim()
    compatibleUnitSymbols = constraintAnnotation.compatibleUnitSymbols().collect({ it.trim() })

    isExactUnitSymbolSpecified = !exactUnitSymbol.isEmpty()
    areCompatibleUnitSymbolsSpecified = !(compatibleUnitSymbols.isEmpty() | compatibleUnitSymbols.any({ it.isEmpty() }))

    if (!isExactUnitSymbolSpecified && !areCompatibleUnitSymbolsSpecified) {
      throw new AssertionError("Either 'exactUnitSymbol' or 'compatibleUnitSymbols' have to be specified." as Object)
    }

    if (isExactUnitSymbolSpecified && areCompatibleUnitSymbolsSpecified) {
      throw new AssertionError("Only one of 'exactUnitSymbol' or 'compatibleUnitSymbols' can be specified." as Object)
    }

    if (isExactUnitSymbolSpecified) {
      try {
        exactUnit = KwrkSimpleUnitFormat.instance.parse(exactUnitSymbol)
      }
      catch (RuntimeException e) { // codenarc-disable-line CatchRuntimeException
        throw new AssertionError("Specified 'exactUnitSymbol' of '${ exactUnitSymbol }' is not recognized.", e)
      }
    }

    if (areCompatibleUnitSymbolsSpecified) {
      try {
        firstCompatibleUnit = KwrkSimpleUnitFormat.instance.parse(compatibleUnitSymbols[0])
      }
      catch (RuntimeException e) { // codenarc-disable-line CatchRuntimeException
        throw new AssertionError("The first specified unit symbol of '${ compatibleUnitSymbols[0] }' in 'compatibleUnitSymbols' is not recognized.", e)
      }
    }
  }

  @Override
  boolean isValid(Quantity quantity, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (quantity == null) {
      return true
    }

    if (isExactUnitSymbolSpecified) {
      if (quantity.unit == exactUnit) {
        return true
      }
    }

    if (areCompatibleUnitSymbolsSpecified) {
      if (quantity.unit.isCompatible(firstCompatibleUnit)) {
        return true
      }
    }

    prepareConstraintValidatorContextForMessageInterpolation(context)
    return false
  }

  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  protected void prepareConstraintValidatorContextForMessageInterpolation(ConstraintValidatorContext context) {
    HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)
    if (message.isEmpty()) {
      // Prevent adding constraint violation with default message if it is empty
      context.disableDefaultConstraintViolation()

      if (isExactUnitSymbolSpecified) {
        hibernateContext.addExpressionVariable("specifiedExactUnitSymbol", exactUnitSymbol)
        hibernateContext.buildConstraintViolationWithTemplate("{${ QuantityUnitConstraint.INVALID_EXACT_UNIT_SYMBOL_MESSAGE_KEY }}").enableExpressionLanguage().addConstraintViolation()
      }

      if (areCompatibleUnitSymbolsSpecified) {
        hibernateContext.addExpressionVariable("specifiedCompatibleUnitSymbols", compatibleUnitSymbols.join(", "))
        hibernateContext.buildConstraintViolationWithTemplate("{${ QuantityUnitConstraint.INVALID_COMPATIBLE_UNIT_SYMBOL_MESSAGE_KEY }}").enableExpressionLanguage().addConstraintViolation()
      }
    }
    else {
      hibernateContext.addExpressionVariable("specifiedExactUnitSymbol", exactUnitSymbol)
      hibernateContext.addExpressionVariable("specifiedCompatibleUnitSymbols", compatibleUnitSymbols.join(", "))
      hibernateContext.buildConstraintViolationWithTemplate(message).enableExpressionLanguage().addConstraintViolation()
    }
  }
}
