package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.klokwrk.lib.validation.constraint.UuidFormatConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import java.util.regex.Pattern

/**
 * Validates if {@code String} is a well-formed UUID.
 * <p/>
 * Null and empty values are ignored (reported as valid). Validation is based on {@link CommonConstants#REGEX_UUID_FORMAT} regex.
 */
@CompileStatic
class UuidFormatConstraintValidator implements ConstraintValidator<UuidFormatConstraint, String> {
  Pattern uuidFormatPattern = ~CommonConstants.REGEX_UUID_FORMAT

  @Override
  boolean isValid(String uuidToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (uuidToValidate == null || uuidToValidate.trim() == "") {
      return true
    }

    if (uuidToValidate ==~ uuidFormatPattern) {
      return true
    }

    return false
  }
}
