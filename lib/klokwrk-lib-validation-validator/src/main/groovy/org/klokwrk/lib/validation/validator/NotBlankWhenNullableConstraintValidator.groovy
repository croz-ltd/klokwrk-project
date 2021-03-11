package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code String} is blank.
 * <p/>
 * Null values are ignored (reported as valid).
 * <p/>
 * This constraint validator is very similar to the bean validation's {@code NotBlank}, but this one skips null values and does not report them as violations.
 */
@CompileStatic
class NotBlankWhenNullableConstraintValidator implements ConstraintValidator<NotBlankWhenNullableConstraint, String> {
  @Override
  boolean isValid(String stringToValidate, ConstraintValidatorContext context) {
    if (stringToValidate == null) {
      return true
    }

    if (stringToValidate.trim() != "") {
      return true
    }

    return false
  }
}
