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
import org.klokwrk.lib.uom.format.KwrkQuantityFormat
import org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint
import tech.units.indriya.ComparableQuantity

import javax.measure.Quantity
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@CompileStatic
class QuantityMaxConstraintValidator implements ConstraintValidator<QuantityMaxConstraint, Quantity> {
  String message
  String maxQuantityParameterString
  Quantity maxQuantityParameter
  boolean inclusiveParameter
  boolean acceptOnlyEqualUnitParameter

  @SuppressWarnings("DuplicatedCode")
  @Override
  void initialize(QuantityMaxConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()
    inclusiveParameter = constraintAnnotation.inclusive()
    acceptOnlyEqualUnitParameter = constraintAnnotation.acceptOnlyEqualUnit()

    maxQuantityParameterString = constraintAnnotation.maxQuantity().trim()
    if (maxQuantityParameterString.isEmpty()) {
      throw new AssertionError("The 'maxQuantity' parameter must be specified." as Object)
    }

    try {
      maxQuantityParameter = KwrkQuantityFormat.instance.parse(maxQuantityParameterString)
    }
    catch (RuntimeException re) { // codenarc-disable-line CatchRuntimeException
      throw new AssertionError("Specified 'maxQuantity' of '${ maxQuantityParameterString }' is not acceptable.", re)
    }
  }

  @Override
  boolean isValid(Quantity validatedQuantity, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (validatedQuantity == null) {
      return true
    }

    if (acceptOnlyEqualUnitParameter) {
      if (validatedQuantity.unit != maxQuantityParameter.unit) {
        prepareMessageInterpolation(validatedQuantity, QuantityMaxConstraint.INVALID_UNIT_EQUAL_KEY, context)
        return false
      }
    }
    else {
      if (!validatedQuantity.unit.isCompatible(maxQuantityParameter.unit)) {
        prepareMessageInterpolation(validatedQuantity, QuantityMaxConstraint.INVALID_UNIT_COMPATIBLE_KEY, context)
        return false
      }
    }

    ComparableQuantity convertedValidatedQuantity = validatedQuantity.to(maxQuantityParameter.unit) as ComparableQuantity
    if (inclusiveParameter) {
      if (convertedValidatedQuantity.isLessThanOrEqualTo(maxQuantityParameter)) {
        return true
      }

      prepareMessageInterpolation(validatedQuantity, QuantityMaxConstraint.INVALID_QUANTITY_INCLUSIVE_KEY, context)
    }
    else {
      if (convertedValidatedQuantity.isLessThan(maxQuantityParameter)) {
        return true
      }

      prepareMessageInterpolation(validatedQuantity, QuantityMaxConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY, context)
    }

    return false
  }

  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  protected void prepareMessageInterpolation(Quantity validatedQuantity, String messageKey, ConstraintValidatorContext context) {
    HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)

    String messageTemplate = message
    if (message.isEmpty()) {
      messageTemplate = "{${ messageKey }}"

      // Prevent adding constraint violation with default message if it is empty
      context.disableDefaultConstraintViolation()
    }

    hibernateContext.addExpressionVariable("expectedMaxQuantity", maxQuantityParameterString)
    hibernateContext.addExpressionVariable("expectedMaxQuantityUnit", maxQuantityParameter.unit.toString())
    hibernateContext.addExpressionVariable("providedQuantity", KwrkQuantityFormat.instance.format(validatedQuantity))
    hibernateContext.addExpressionVariable("providedQuantityUnit", validatedQuantity.unit.toString())

    hibernateContext.buildConstraintViolationWithTemplate(messageTemplate).enableExpressionLanguage().addConstraintViolation()
  }
}
