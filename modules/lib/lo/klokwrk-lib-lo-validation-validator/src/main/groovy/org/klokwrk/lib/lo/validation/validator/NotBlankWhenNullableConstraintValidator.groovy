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
import org.klokwrk.lib.lo.validation.constraint.NotBlankWhenNullableConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

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
