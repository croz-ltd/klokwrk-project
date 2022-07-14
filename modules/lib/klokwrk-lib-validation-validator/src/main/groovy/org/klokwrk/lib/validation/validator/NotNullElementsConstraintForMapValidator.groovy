package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code Map} instance contains {@code null} keys and/or values.
 * <p/>
 * {@code null} or empty {@code Maps} are ignored (reported as valid).
 */
@CompileStatic
class NotNullElementsConstraintForMapValidator implements ConstraintValidator<NotNullElementsConstraint, Map> {
  @Override
  boolean isValid(Map map, ConstraintValidatorContext context) {
    if (map == null) {
      return true
    }

    if (map.isEmpty()) {
      return true
    }

    if (map.containsKey(null)) {
      return false
    }

    if (map.containsValue(null)) {
      return false
    }

    return true
  }
}
