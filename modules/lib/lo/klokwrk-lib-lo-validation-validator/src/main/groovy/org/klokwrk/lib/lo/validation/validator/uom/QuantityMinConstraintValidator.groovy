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
package org.klokwrk.lib.lo.validation.validator.uom

import groovy.transform.CompileStatic
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.klokwrk.lib.lo.uom.format.KwrkQuantityFormat
import org.klokwrk.lib.lo.validation.constraint.uom.QuantityMinConstraint
import tech.units.indriya.ComparableQuantity

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import javax.measure.Quantity

@CompileStatic
class QuantityMinConstraintValidator implements ConstraintValidator<QuantityMinConstraint, Quantity> {
  String message
  String minQuantityParameterString
  Quantity minQuantityParameter
  boolean inclusiveParameter
  boolean acceptOnlyEqualUnitParameter

  @Override
  void initialize(QuantityMinConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()
    inclusiveParameter = constraintAnnotation.inclusive()
    acceptOnlyEqualUnitParameter = constraintAnnotation.acceptOnlyEqualUnit()

    minQuantityParameterString = constraintAnnotation.minQuantity().trim()
    if (minQuantityParameterString.isEmpty()) {
      throw new AssertionError("The 'minQuantity' parameter must be specified." as Object)
    }

    try {
      minQuantityParameter = KwrkQuantityFormat.instance.parse(minQuantityParameterString)
    }
    catch (RuntimeException re) { // codenarc-disable-line CatchRuntimeException
      throw new AssertionError("Specified 'minQuantity' of '${ minQuantityParameterString }' is not acceptable.", re)
    }
  }

  @Override
  boolean isValid(Quantity validatedQuantity, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (validatedQuantity == null) {
      return true
    }

    if (acceptOnlyEqualUnitParameter) {
      if (validatedQuantity.unit != minQuantityParameter.unit) {
        prepareMessageInterpolation(validatedQuantity, QuantityMinConstraint.INVALID_UNIT_EQUAL_KEY, context)
        return false
      }
    }
    else {
      if (!validatedQuantity.unit.isCompatible(minQuantityParameter.unit)) {
        prepareMessageInterpolation(validatedQuantity, QuantityMinConstraint.INVALID_UNIT_COMPATIBLE_KEY, context)
        return false
      }
    }

    ComparableQuantity convertedValidatedQuantity = validatedQuantity.toComparable(minQuantityParameter.unit)
    if (inclusiveParameter) {
      if (convertedValidatedQuantity.isGreaterThanOrEqualTo(minQuantityParameter)) {
        return true
      }

      prepareMessageInterpolation(validatedQuantity, QuantityMinConstraint.INVALID_QUANTITY_INCLUSIVE_KEY, context)
    }
    else {
      if (convertedValidatedQuantity.isGreaterThan(minQuantityParameter)) {
        return true
      }

      prepareMessageInterpolation(validatedQuantity, QuantityMinConstraint.INVALID_QUANTITY_EXCLUSIVE_KEY, context)
    }

    return false
  }

  @SuppressWarnings(["DuplicatedCode", "CodeNarc.DuplicateStringLiteral"])
  protected void prepareMessageInterpolation(Quantity validatedQuantity, String messageKey, ConstraintValidatorContext context) {
    HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)

    String messageTemplate = message
    if (message.isEmpty()) {
      messageTemplate = "{${ messageKey }}"

      // Prevent adding constraint violation with default message if it is empty
      context.disableDefaultConstraintViolation()
    }

    hibernateContext.addExpressionVariable("expectedMinQuantity", minQuantityParameterString)
    hibernateContext.addExpressionVariable("expectedMinQuantityUnit", minQuantityParameter.unit.toString())
    hibernateContext.addExpressionVariable("providedQuantity", KwrkQuantityFormat.instance.format(validatedQuantity))
    hibernateContext.addExpressionVariable("providedQuantityUnit", validatedQuantity.unit.toString())

    hibernateContext.buildConstraintViolationWithTemplate(messageTemplate).enableExpressionLanguage().addConstraintViolation()
  }
}
