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
package org.klokwrk.lib.lo.validation.validator

import groovy.transform.CompileStatic
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validates if {@code String} is a well-formed random UUID.
 * <p/>
 * {@code null} and empty values are ignored (reported as valid). Validation is based on {@code UUID.fromString()} method.
 */
@CompileStatic
class RandomUuidFormatConstraintValidator implements ConstraintValidator<RandomUuidFormatConstraint, String> {
  String message

  @Override
  void initialize(RandomUuidFormatConstraint constraintAnnotation) {
    message = constraintAnnotation.message().trim()
  }

  @SuppressWarnings("CodeNarc.CatchException")
  @Override
  boolean isValid(String uuidStringToValidate, ConstraintValidatorContext context) {
    UUID parsedUuid

    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (uuidStringToValidate == null || uuidStringToValidate.trim() == "") {
      return true
    }

    try {
      parsedUuid = UUID.fromString(uuidStringToValidate)
    }
    catch (Exception ignore) {
      configureContextAndAddConstraintViolation(message, context, "{${ RandomUuidFormatConstraint.INVALID_UUID_FORMAT_MESSAGE_KEY }}")
      return false
    }

    boolean isValidRandomUuid = (parsedUuid.version() == 4) && (parsedUuid.variant() == 2)
    if (!isValidRandomUuid) {
      configureContextAndAddConstraintViolation(message, context, "{${ RandomUuidFormatConstraint.INVALID_RANDOM_UUID_FORMAT_MESSAGE_KEY }}")
      return false
    }

    return true
  }

  protected void configureContextAndAddConstraintViolation(String annotationMessage, ConstraintValidatorContext context, String templateToUseWhenAnnotationMessageIsEmpty) {
    if (annotationMessage.isEmpty()) {
      // Prevent adding constraint violation with default message which is empty in this case
      context.disableDefaultConstraintViolation()
      context.buildConstraintViolationWithTemplate(templateToUseWhenAnnotationMessageIsEmpty).addConstraintViolation()
    }
    else {
      HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext)
      hibernateContext.buildConstraintViolationWithTemplate(annotationMessage).enableExpressionLanguage().addConstraintViolation()
    }
  }
}
