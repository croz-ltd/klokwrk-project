package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code Collection} contains {@code null} elements.
 * <p/>
 * {@code null} or empty {@code Collections} are ignored (reported as valid).
 */
@CompileStatic
class NotNullElementsConstraintForCollectionValidator implements ConstraintValidator<NotNullElementsConstraint, Collection> {
  @Override
  boolean isValid(Collection collection, ConstraintValidatorContext context) {
    if (collection == null) {
      return true
    }

    if (collection.isEmpty()) {
      return true
    }

    if (collection.contains(null)) {
      return false
    }

    return true
  }
}
