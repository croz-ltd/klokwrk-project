package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code Map} is empty.
 * <p/>
 * Null {@code Maps} are ignored (reported as valid).
 * <p/>
 * This constraint validator is very similar to the standard bean validation's {@code NotEmpty}, but this one skips null {@code Maps} and does not report them as violations.
 */
@CompileStatic
class NotEmptyWhenNullableConstraintForMapValidator implements ConstraintValidator<NotEmptyWhenNullableConstraint, Map> {
  @Override
  boolean isValid(Map map, ConstraintValidatorContext context) {
    if (map == null) {
      return true
    }

    return !map.isEmpty()
  }
}
