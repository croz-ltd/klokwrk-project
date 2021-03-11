package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lang.groovy.constant.CommonConstants
import org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import java.util.regex.Pattern

/**
 * Validates if {@code String} is a well-formed UN/LOCODE.
 * <p/>
 * Null and empty values are ignored (reported as valid). Validation is based on {@link CommonConstants#REGEX_UN_LO_CODE} regex.
 */
@CompileStatic
class UnLoCodeFormatConstraintValidator implements ConstraintValidator<UnLoCodeFormatConstraint, String> {
  Pattern unLoCodeFormatPattern = ~CommonConstants.REGEX_UN_LO_CODE

  @Override
  boolean isValid(String unLoCodeToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (unLoCodeToValidate == null || unLoCodeToValidate.trim() == "") {
      return true
    }

    if (unLoCodeToValidate ==~ unLoCodeFormatPattern) {
      return true
    }

    return false
  }
}
