package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.klokwrk.lib.validation.constraint.ValueOfEnumConstraint

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * {@link ConstraintValidator} implementation of {@link ValueOfEnumConstraint} for validating {@code String} values.
 * <p/>
 * {@code null} and empty {@code String} values are ignored (reported as valid).
 * <p/>
 * If {@code enumNamesSubset} does not contain an enumeration name from {@code enumClass} (ignoring case), {@code AssertionError} is thrown during initialization.
 */
@CompileStatic
class ValueOfEnumConstraintValidator implements ConstraintValidator<ValueOfEnumConstraint, String> {
  Class<? extends Enum> enumClass
  boolean isValidatingSubsetOfEnum = false
  List<String> enumNamesToCheckAgainst
  String message

  @Override
  void initialize(ValueOfEnumConstraint constraintAnnotation) {
    enumClass = constraintAnnotation.enumClass()
    message = constraintAnnotation.message().trim()

    List<String> allEnumNames = constraintAnnotation.enumClass().enumConstants.collect({ Enum anEnum -> anEnum.name() })
    List<String> subsetOfEnumNames = constraintAnnotation.enumNamesSubset().collect({ String enumName -> enumName.toUpperCase() })

    subsetOfEnumNames.each({ String enumName ->
      if (!allEnumNames.contains(enumName)) {
        throw new AssertionError("Subset value of '${ enumName }' is not part of ${ enumClass.name } enum." as Object)
      }
    })

    enumNamesToCheckAgainst = allEnumNames

    if (!subsetOfEnumNames.isEmpty()) {
      isValidatingSubsetOfEnum = true
      enumNamesToCheckAgainst = subsetOfEnumNames
    }
  }

  @SuppressWarnings("CodeNarc.DuplicateStringLiteral")
  @Override
  boolean isValid(String enumNameToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (enumNameToValidate == null || enumNameToValidate.trim() == "") {
      return true
    }

    if (enumNamesToCheckAgainst.contains(enumNameToValidate.toUpperCase())) {
      return true
    }

    HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)
    if (message.isEmpty()) {
      // Prevent adding constraint violation with default message if it is empty
      context.disableDefaultConstraintViolation()

      if (isValidatingSubsetOfEnum) {
        hibernateContext.addExpressionVariable("enumNamesSubsetList", enumNamesToCheckAgainst.join(", "))
        hibernateContext.buildConstraintViolationWithTemplate("{${ ValueOfEnumConstraint.INVALID_SUBSET_OF_ENUM_VALUE_MESSAGE_KEY }}").enableExpressionLanguage().addConstraintViolation()
      }
      else {
        hibernateContext.addExpressionVariable("enumClassSimpleName", enumClass.simpleName)
        hibernateContext.buildConstraintViolationWithTemplate("{${ ValueOfEnumConstraint.INVALID_ENUM_VALUE_MESSAGE_KEY }}").enableExpressionLanguage().addConstraintViolation()
      }
    }
    else {
      hibernateContext.addExpressionVariable("enumClassSimpleName", enumClass.simpleName)
      hibernateContext.addExpressionVariable("enumNamesSubsetList", enumNamesToCheckAgainst.join(", "))
      hibernateContext.buildConstraintViolationWithTemplate(message).enableExpressionLanguage().addConstraintViolation()
    }

    return false
  }
}
