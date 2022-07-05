package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code Collection} is empty.
 * <p/>
 * Null {@code Collections} are ignored (reported as valid).
 * <p/>
 * This constraint validator is very similar to the standard bean validation's {@code NotEmpty}, but this one skips null {@code Collections} and does not report them as violations.
 */
@CompileStatic
class NotEmptyWhenNullableConstraintForCollectionValidator implements ConstraintValidator<NotEmptyWhenNullableConstraint, Collection> {
  @Override
  boolean isValid(Collection collection, ConstraintValidatorContext context) {
    if (collection == null) {
      return true
    }

    return !collection.isEmpty()
  }
}
