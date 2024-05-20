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

import groovy.transform.CompileStatic
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.klokwrk.lib.lo.uom.format.KwrkQuantityFormat
import org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint
import tech.units.indriya.ComparableQuantity

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import javax.measure.Quantity

@CompileStatic
class QuantityRangeConstraintValidator implements ConstraintValidator<QuantityRangeConstraint, Quantity> {
  String message

  String minQuantityParameterString
  Quantity minQuantityParameter
  boolean minInclusiveParameter

  String maxQuantityParameterString
  Quantity maxQuantityParameter
  boolean maxInclusiveParameter

  boolean acceptOnlyEqualUnitParameter

  @SuppressWarnings("DuplicatedCode")
  @Override
  void initialize(QuantityRangeConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()

    acceptOnlyEqualUnitParameter = constraintAnnotation.acceptOnlyEqualUnit()

    minInclusiveParameter = constraintAnnotation.minInclusive()
    maxInclusiveParameter = constraintAnnotation.maxInclusive()

    minQuantityParameterString = constraintAnnotation.minQuantity().trim()
    if (minQuantityParameterString.isEmpty()) {
      throw new AssertionError("The 'minQuantity' parameter must be specified." as Object)
    }

    maxQuantityParameterString = constraintAnnotation.maxQuantity().trim()
    if (maxQuantityParameterString.isEmpty()) {
      throw new AssertionError("The 'maxQuantity' parameter must be specified." as Object)
    }

    try {
      minQuantityParameter = KwrkQuantityFormat.instance.parse(minQuantityParameterString)
    }
    catch (RuntimeException re) { // codenarc-disable-line CatchRuntimeException
      throw new AssertionError("Specified 'minQuantity' of '${ minQuantityParameterString }' is not acceptable.", re)
    }

    try {
      maxQuantityParameter = KwrkQuantityFormat.instance.parse(maxQuantityParameterString)
    }
    catch (RuntimeException re) { // codenarc-disable-line CatchRuntimeException
      throw new AssertionError("Specified 'maxQuantity' of '${ maxQuantityParameterString }' is not acceptable.", re)
    }

    if (acceptOnlyEqualUnitParameter) {
      if (minQuantityParameter.unit != maxQuantityParameter.unit) {
        throw new AssertionError("When 'acceptOnlyEqualUnit' is set to 'true', both min and max quantities must use equal units." as Object)
      }
    }
    else {
      if (!minQuantityParameter.unit.isCompatible(maxQuantityParameter.unit)) {
        throw new AssertionError("The units of 'minQuantity' and 'maxQuantity' must be mutually compatible." as Object)
      }
    }
  }

  @Override
  boolean isValid(Quantity validatedQuantity, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (validatedQuantity == null) {
      return true
    }

    if (acceptOnlyEqualUnitParameter) {
      // In case of equal unit, both min and max quantities must use equal units. This is checked during validator initialization.
      // Therefore, we can use either min or max quantity parameter for the check.
      if (validatedQuantity.unit != minQuantityParameter.unit) {
        prepareMessageInterpolation(validatedQuantity, QuantityRangeConstraint.INVALID_UNIT_EQUAL_KEY, context)
        return false
      }
    }
    else {
      if (!validatedQuantity.unit.isCompatible(minQuantityParameter.unit)) {
        prepareMessageInterpolation(validatedQuantity, QuantityRangeConstraint.INVALID_UNIT_COMPATIBLE_KEY, context)
        return false
      }
    }

    ComparableQuantity convertedMinValidatedQuantity = validatedQuantity.toComparable(minQuantityParameter.unit)
    ComparableQuantity convertedMaxValidatedQuantity = validatedQuantity.toComparable(maxQuantityParameter.unit)

    boolean isMinBoundSatisfied = minInclusiveParameter ? convertedMinValidatedQuantity.isGreaterThanOrEqualTo(minQuantityParameter) : convertedMinValidatedQuantity.isGreaterThan(minQuantityParameter)
    boolean isMaxBoundSatisfied = maxInclusiveParameter ? convertedMaxValidatedQuantity.isLessThanOrEqualTo(maxQuantityParameter) : convertedMaxValidatedQuantity.isLessThan(maxQuantityParameter)

    if (isMinBoundSatisfied && isMaxBoundSatisfied) {
      return true
    }

    prepareMessageInterpolation(validatedQuantity, determineMessageKeyForUnsatisfiedBounds(), context)

    return false
  }

  @SuppressWarnings(["DuplicatedCode", "CodeNarc.UnnecessaryObjectReferences"])
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
    hibernateContext.addExpressionVariable("expectedMaxQuantity", maxQuantityParameterString)
    hibernateContext.addExpressionVariable("expectedMaxQuantityUnit", maxQuantityParameter.unit.toString())
    hibernateContext.addExpressionVariable("providedQuantity", KwrkQuantityFormat.instance.format(validatedQuantity))
    hibernateContext.addExpressionVariable("providedQuantityUnit", validatedQuantity.unit.toString())

    hibernateContext.buildConstraintViolationWithTemplate(messageTemplate).enableExpressionLanguage().addConstraintViolation()
  }

  protected String determineMessageKeyForUnsatisfiedBounds() {
    String messageKey
    if (minInclusiveParameter) {
      if (maxInclusiveParameter) {
        messageKey = QuantityRangeConstraint.INVALID_QUANTITY_MIN_INCLUSIVE_MAX_INCLUSIVE_KEY
      }
      else {
        messageKey = QuantityRangeConstraint.INVALID_QUANTITY_MIN_INCLUSIVE_MAX_EXCLUSIVE_KEY
      }
    }
    else {
      if (maxInclusiveParameter) {
        messageKey = QuantityRangeConstraint.INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_INCLUSIVE_KEY
      }
      else {
        messageKey = QuantityRangeConstraint.INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_EXCLUSIVE_KEY
      }
    }

    return messageKey
  }
}
