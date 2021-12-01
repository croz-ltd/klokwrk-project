package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validates if {@code String} is a well-formed random UUID.
 * <p/>
 * {@code null} and empty values are ignored (reported as valid). Validation is based on {@code UUID.fromString()} method.
 */
@CompileStatic
class RandomUuidFormatConstraintValidator implements ConstraintValidator<RandomUuidFormatConstraint, String> {
  @SuppressWarnings("CodeNarc.CatchException")
  @Override
  boolean isValid(String uuidStringToValidate, ConstraintValidatorContext context) {
    UUID parsedUuid

    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (uuidStringToValidate == null || uuidStringToValidate.trim() == "") {
      return true
    }

    // Prevent adding constraint violation with default message which is empty
    context.disableDefaultConstraintViolation()

    try {
      parsedUuid = UUID.fromString(uuidStringToValidate)
    }
    catch (Exception ignore) {
      context.buildConstraintViolationWithTemplate("{${ RandomUuidFormatConstraint.INVALID_UUID_FORMAT_MESSAGE_KEY }}").addConstraintViolation()
      return false
    }

    Boolean isValidRandomUuid = (parsedUuid.version() == 4) && (parsedUuid.variant() == 2)
    if (!isValidRandomUuid) {
      context.buildConstraintViolationWithTemplate("{${ RandomUuidFormatConstraint.INVALID_RANDOM_UUID_FORMAT_MESSAGE_KEY }}").addConstraintViolation()
      return false
    }

    return true
  }
}
